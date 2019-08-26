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
import static org.openhab.binding.tuya.internal.json.CommandByte.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.CommandDispatcher;
import org.openhab.binding.tuya.internal.DeviceDescriptor;
import org.openhab.binding.tuya.internal.DeviceRepository;
import org.openhab.binding.tuya.internal.json.JsonDiscovery;
import org.openhab.binding.tuya.internal.net.DeviceEventEmitter;
import org.openhab.binding.tuya.internal.net.DeviceEventEmitter.Event;
import org.openhab.binding.tuya.internal.net.Message;
import org.openhab.binding.tuya.internal.util.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmbilightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 */
public abstract class AbstractTuyaHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(AbstractTuyaHandler.class);

    protected MessageParser parser;
    protected String id;

    protected DeviceDescriptor deviceDescriptor;
    protected DeviceEventEmitter deviceEventEmitter;
    protected final CommandDispatcher commandDispatcher;

    public AbstractTuyaHandler(Thing thing) {
        super(thing);
        commandDispatcher = new CommandDispatcher(thing.getUID());
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
    }

    /**
     * Dispose of allocated resources.
     */
    @Override
    public void dispose() {
        if (deviceEventEmitter != null) {
            deviceEventEmitter.stop();
        }
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
     */
    private void deviceFound(DeviceDescriptor device) {
        if (device != null) {
            JsonDiscovery jd = device.getJsonDiscovery();
            if (jd != null && jd.getGwId() != null && jd.getGwId().equals(id)) {
                if (deviceDescriptor == null || !deviceDescriptor.getIp().equals(jd.getIp())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, device.getIp());
                    deviceDescriptor = device;
                    updateProperties(false);
                    thing.getConfiguration().put("ip", device.getIp());
                    deviceEventEmitter = new DeviceEventEmitter(device.getIp(), DEFAULT_SERVER_PORT, parser);

                    // Handle error events
                    deviceEventEmitter.on(Event.CONNECTION_ERROR, (ev, msg) -> {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg.getData());
                        return true;
                    });

                    // Handle connected event.
                    deviceEventEmitter.on(Event.CONNECTED, (ev, msg) -> {
                        updateStatus(ThingStatus.ONLINE);
                        updateProperties(false);
                        sendStatusQuery();
                        return true;
                    });

                    // Handle messages received.
                    deviceEventEmitter.on(DeviceEventEmitter.Event.MESSAGE_RECEIVED, (ev, msg) -> {
                        if (msg.getCommandByte() == STATUS || msg.getCommandByte() == DP_QUERY) {
                            handleStatusMessage(msg);
                        } else if (msg.getCommandByte() != HEART_BEAT) {
                            handleStatusMessage(msg);
                        }
                        return true;
                    });

                    // Start the event emitter.
                    deviceEventEmitter.start(scheduler);
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
        if (!commandDispatcher.dispatchCommand(deviceEventEmitter, channelUID, command, CONTROL)) {
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
        String key = config.get("key").toString();
        String version = config.get("version").toString();
        parser = new MessageParser(version, key);
        String ip = (String) config.get("ip");

        // If ip-address is specified, try to use it.
        if (ip != null && !ip.isEmpty()) {
            deviceFound(new DeviceDescriptor(new JsonDiscovery(id, version, ip)));
        }

        // Initialize auto-discovery of the ip-address.
        DeviceRepository.getInstance().on(id, (ev, device) -> {
            deviceFound(device);
            return true;
        });

        // Init dispatcher.
        initCommandDispatcher();

    }

}
