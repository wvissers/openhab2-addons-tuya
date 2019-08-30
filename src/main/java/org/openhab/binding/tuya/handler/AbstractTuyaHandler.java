/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.handler;

import static org.openhab.binding.tuya.TuyaBindingConstants.*;
import static org.openhab.binding.tuya.internal.data.CommandByte.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.data.CommandByte;
import org.openhab.binding.tuya.internal.data.DeviceState;
import org.openhab.binding.tuya.internal.data.Message;
import org.openhab.binding.tuya.internal.data.StatusQuery;
import org.openhab.binding.tuya.internal.discovery.DeviceDescriptor;
import org.openhab.binding.tuya.internal.discovery.DeviceRepository;
import org.openhab.binding.tuya.internal.discovery.JsonDiscovery;
import org.openhab.binding.tuya.internal.exceptions.HandlerInitializationException;
import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.exceptions.UnsupportedVersionException;
import org.openhab.binding.tuya.internal.net.TcpConfig;
import org.openhab.binding.tuya.internal.net.TuyaClient;
import org.openhab.binding.tuya.internal.net.TuyaClient.Event;
import org.openhab.binding.tuya.internal.util.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link AmbilightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 */
public abstract class AbstractTuyaHandler extends BaseThingHandler implements TcpConfig {

    private Logger logger = LoggerFactory.getLogger(AbstractTuyaHandler.class);

    protected String id;

    protected DeviceDescriptor deviceDescriptor;
    protected TuyaClient tuyaClient;
    protected final CommandDispatcher commandDispatcher;

    public AbstractTuyaHandler(Thing thing) {
        super(thing);
        commandDispatcher = new CommandDispatcher(thing.getUID());
    }

    /**
     * Update the states of channels that are changed.
     *
     * @param dev the device data.
     */
    protected void updateStates(Message message, Class<? extends DeviceState> clazz) {
        if (message != null && message.getData() != null && message.getData().startsWith("{")) {
            try {
                DeviceState dev = message.toDeviceState(clazz);
                if (dev != null) {
                    dev.forChangedProperties((channel, state) -> {
                        updateState(new ChannelUID(thing.getUID(), channel), state);
                    });
                }
            } catch (JsonSyntaxException e) {
                logger.error("Statusmessage invalid", e);
            }
        }
    }

    /**
     * This method is called when a DeviceEventEmitter.Event.MESSAGE_RECEIVED is received from the device. In
     * subclasses, this should result in a possible state change of the things channels.
     */
    protected void handleStatusMessage(Message message) {
    }

    /**
     * This method is called when the device is connected, for an initial status request if the device supports it.
     */
    protected void sendStatusQuery() {
        try {
            StatusQuery query = new StatusQuery(deviceDescriptor);
            tuyaClient.send(query, CommandByte.DP_QUERY);
        } catch (IOException | ParseException e) {
            logger.error("Error on status request", e);
        }
    }

    /**
     * Dispose of allocated resources.
     */
    @Override
    public void dispose() {
        if (tuyaClient != null) {
            tuyaClient.stop();
            tuyaClient = null;
        }
        if (deviceDescriptor != null && deviceDescriptor.getGwId() != null) {
            DeviceRepository.getInstance().removeHandler(deviceDescriptor.getGwId());
        }
        if (commandDispatcher != null) {
            commandDispatcher.removeAllHandlers();
        }
        deviceDescriptor = null;
    }

    /**
     * Update the properties that can be inspected with e.g. the Paper UI. May be
     * overridden in subclasses to add more specific device properties.
     */
    protected void updateProperties(boolean clear) {
        thing.setProperty(PROPERTY_VERSION, clear ? "" : deviceDescriptor.getVersion());
        thing.setProperty(PROPERTY_IP_ADDRESS, clear ? "" : deviceDescriptor.getIp());
        thing.setProperty(PROPERTY_PRODUCT_KEY, clear ? "" : deviceDescriptor.getProductKey());
    }

    /**
     * Handle a device found by the discovery service. In particular, set or update the IP address.
     *
     * @param device the device descriptor, received from the DeviceRepository service.
     * @throws UnsupportedVersionException
     */
    private void deviceFound(DeviceDescriptor device) throws UnsupportedVersionException {
        if (device != null) {
            JsonDiscovery jd = device.getJsonDiscovery();
            if (jd != null && jd.getGwId() != null && jd.getGwId().equals(id)) {
                if (deviceDescriptor == null || !deviceDescriptor.getIp().equals(jd.getIp())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, device.getIp());
                    deviceDescriptor = device;
                    updateProperties(false);
                    thing.getConfiguration().put("ip", device.getIp());
                    tuyaClient = new TuyaClient(device.getIp(), DEFAULT_SERVER_PORT, device.getVersion(),
                            device.getLocalKey());

                    // Handle error events
                    tuyaClient.on(Event.CONNECTION_ERROR, (ev, msg) -> {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        return true;
                    });

                    // Handle connected event.
                    tuyaClient.on(Event.CONNECTED, (ev, msg) -> {
                        updateStatus(ThingStatus.ONLINE);
                        updateProperties(false);
                        // Ask status after some delay to let the items be created first.
                        scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                sendStatusQuery();
                            }
                        }, STATUS_REQUEST_DELAY_SECONDS, TimeUnit.SECONDS);
                        return true;
                    });

                    // Handle messages received.
                    tuyaClient.on(Event.MESSAGE_RECEIVED, (ev, msg) -> {
                        if (msg.getCommandByte() == STATUS || msg.getCommandByte() == DP_QUERY) {
                            handleStatusMessage(msg);
                        }
                        return true;
                    });

                    // Start the client.
                    tuyaClient.start(scheduler);
                }
            }
        }
    }

    /**
     * Handle specific commands for this type of device. Subclasses should initialize the command dispatcher with device
     * specific commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!commandDispatcher.dispatchCommand(tuyaClient, channelUID, command, CONTROL)) {
            logger.info("Command {} for channel {} could not be handled.", command, channelUID);
        }
    }

    /**
     * Subclasses should add the commands to the dispatcher.
     */
    protected void initCommandDispatcher() {
    }

    @Override
    public void initialize() {

        // Dispose of allocated resources when re-initializing.
        dispose();

        // Get the configuration object.
        Configuration config = thing.getConfiguration();

        // Clear properties.
        updateProperties(true);

        id = config.get("id").toString();
        String localKey = config.get("key").toString();
        String version = config.get("version").toString();
        String ip = (String) config.get("ip");

        // If ip-address is specified, try to use it.
        if (ip != null && !ip.isEmpty()) {
            try {
                deviceFound(new DeviceDescriptor(new JsonDiscovery(id, version, ip)).withLocalKey(localKey));
            } catch (UnsupportedVersionException e) {
                throw new HandlerInitializationException(e.getMessage());
            }
        }

        // Initialize auto-discovery of the ip-address.
        try {
            DeviceRepository.getInstance().on(id, (ev, device) -> {
                try {
                    deviceFound(device.withLocalKey(localKey));
                } catch (UnsupportedVersionException e) {
                    throw new HandlerInitializationException(e.getMessage());
                }
                return true;
            });
        } catch (Exception e) {
            throw new HandlerInitializationException("Device ID already assigned to a Tuya thing.");
        }

        // Init dispatcher.
        initCommandDispatcher();

    }

}
