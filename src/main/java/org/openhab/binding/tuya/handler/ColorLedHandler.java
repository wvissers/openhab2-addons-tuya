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

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.data.ColorLedDevice;
import org.openhab.binding.tuya.internal.data.CommandByte;
import org.openhab.binding.tuya.internal.data.Message;
import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.util.Calc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler for a Tuya Switch device.
 *
 * @author Wim Vissers
 *
 */
public class ColorLedHandler extends AbstractTuyaHandler {

    private Logger logger = LoggerFactory.getLogger(ColorLedHandler.class);

    public ColorLedHandler(Thing thing) {
        super(thing);
    }

    /**
     * This method is called when a DeviceEventEmitter.Event.MESSAGE_RECEIVED is received from the device. This could
     * result in a possible state change of this things channels.
     */
    @Override
    protected void handleStatusMessage(Message message) {
        // ColorLedDevice dev = message.toColorLedDevice();
        // updateState(new ChannelUID(thing.getUID(), CHANNEL_POWER), dev.getDps().isDp1() ? OnOffType.ON :
        // OnOffType.OFF);
    }

    /**
     * Handle specific commands for this type of device.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType || command instanceof Number || command instanceof HSBType) {
            try {
                ColorLedDevice dev = new ColorLedDevice(deviceDetails);
                switch (channelUID.getId()) {
                    case CHANNEL_POWER:
                        if (command instanceof OnOffType) {
                            dev.getDps().setDp1(command == OnOffType.ON);
                            String msg = gson.toJson(dev);
                            deviceEventEmitter.set(msg, CommandByte.CONTROL);
                        }
                        break;
                    case CHANNEL_COLOR_MODE:
                        if (command instanceof OnOffType) {
                            dev.getDps().setDp2(command == OnOffType.ON ? "colour" : "white");
                            String msg = gson.toJson(dev);
                            deviceEventEmitter.set(msg, CommandByte.CONTROL);
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof Number) {
                            dev.getDps().setDp3(Calc.numberTo255(command));
                            String msg = gson.toJson(dev);
                            deviceEventEmitter.set(msg, CommandByte.CONTROL);
                        }
                        break;
                    case CHANNEL_COLOR_TEMPERATURE:
                        if (command instanceof Number) {
                            dev.getDps().setDp4(Calc.numberTo255(command));
                            String msg = gson.toJson(dev);
                            deviceEventEmitter.set(msg, CommandByte.CONTROL);
                        }
                        break;
                    case CHANNEL_COLOR:
                        if (command instanceof HSBType) {
                            String x = "8000ff016500ff";
                            dev.getDps().setDp5(Calc.colorToCommandString((HSBType) command));
                            String msg = gson.toJson(dev);
                            deviceEventEmitter.set(msg, CommandByte.CONTROL);
                        }
                    default:
                        logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                        break;
                }
            } catch (IOException | ParseException e) {
                logger.error("Error setting device properties", e);
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

}
