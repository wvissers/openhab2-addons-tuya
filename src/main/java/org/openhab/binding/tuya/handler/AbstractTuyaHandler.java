/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.handler;

import static org.openhab.binding.tuya.internal.Constants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.tuya.internal.DeviceDescriptor;
import org.openhab.binding.tuya.internal.DeviceRepository;
import org.openhab.binding.tuya.internal.data.CommandByte;
import org.openhab.binding.tuya.internal.data.DeviceDatagram;
import org.openhab.binding.tuya.internal.data.Message;
import org.openhab.binding.tuya.internal.net.DeviceEventEmitter;
import org.openhab.binding.tuya.internal.net.DeviceEventEmitter.Event;
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

    protected DeviceDescriptor deviceDetails;
    protected DeviceEventEmitter deviceEventEmitter;

    public AbstractTuyaHandler(Thing thing) {
        super(thing);
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
        DeviceRepository.getInstance().stop();
        if (deviceEventEmitter != null) {
            deviceEventEmitter.stop();
        }
    }

    /**
     * Update the properties that can be inspected with e.g. the Paper UI. May be
     * overridden in subclasses to add more specific device properties.
     */
    protected void updateProperties(boolean clear) {
        thing.setProperty(PROPERTY_VERSION, clear ? "" : deviceDetails.getVersion());
        thing.setProperty(PROPERTY_IP_ADDRESS, clear ? "" : deviceDetails.getIp());
        thing.setProperty(PROPERTY_PRODUCT_KEY, clear ? "" : deviceDetails.getProductKey());
    }

    /**
     * Handle a device found by the discovery service. In particular, set or update the IP address.
     *
     * @param device the device descriptor, received from the DeviceRepository service.
     */
    private void foundDevice(DeviceDescriptor device) {
        if (device != null) {
            DeviceDatagram ddg = device.getDeviceDatagram();
            if (ddg != null && ddg.getGwId() != null && ddg.getGwId().equals(id)) {
                if (deviceDetails == null || !deviceDetails.getIp().equals(ddg.getIp())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, device.getIp());
                    deviceDetails = device;
                    updateProperties(false);
                    deviceEventEmitter = new DeviceEventEmitter(device.getIp(), 6668, parser);

                    // Handle error events
                    deviceEventEmitter.on(Event.CONNECTION_ERROR, msg -> {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg.getData());
                    });

                    // Handle connected event.
                    deviceEventEmitter.on(Event.CONNECTED, msg -> {
                        updateStatus(ThingStatus.ONLINE);
                        updateProperties(false);
                        sendStatusQuery();
                    });

                    // Handle messages received.
                    deviceEventEmitter.on(DeviceEventEmitter.Event.MESSAGE_RECEIVED, message -> {
                        if (message.getCommandByte() == CommandByte.STATUS.getValue()
                                || message.getCommandByte() == CommandByte.DP_QUERY.getValue()) {
                            handleStatusMessage(message);
                        }
                    });

                    // Start the event emitter.
                    deviceEventEmitter.start(scheduler);
                }
            }
        }
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
            foundDevice(new DeviceDescriptor(new DeviceDatagram(id, version, ip)));
        }

        // Initialize auto-discovery of the ip-address.
        DeviceRepository.getInstance().on(DeviceRepository.Event.DEVICE_FOUND, device -> {
            foundDevice(device);
        });

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
