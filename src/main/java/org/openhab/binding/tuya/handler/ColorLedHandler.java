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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.tuya.internal.json.JsonColorLed;
import org.openhab.binding.tuya.internal.net.Message;

/**
 * A handler for a Tuya Switch device.
 *
 * @author Wim Vissers
 *
 */
public class ColorLedHandler extends AbstractTuyaHandler {

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
     * Add the commands to the dispatcher.
     */
    @Override
    protected void initCommandDispatcher() {
        // Channel power command with OnOffType.
        commandDispatcher.on(CHANNEL_POWER, OnOffType.class, command -> {
            JsonColorLed dev = new JsonColorLed(deviceDescriptor);
            dev.getDps().setDp1(command == OnOffType.ON);
            return dev;
        });

        // Color mode command with OnOffType.
        commandDispatcher.on(CHANNEL_COLOR_MODE, OnOffType.class, command -> {
            JsonColorLed dev = new JsonColorLed(deviceDescriptor);
            dev.getDps().setDp2(command == OnOffType.ON ? "colour" : "white");
            return dev;
        });

        // Brightness with DecimalType.
        commandDispatcher.on(CHANNEL_BRIGHTNESS, DecimalType.class, command -> {
            JsonColorLed dev = new JsonColorLed(deviceDescriptor);
            dev.getDps().setDp3(numberTo255(command));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.OFF);
            return dev;
        });

        // Color temperature with DecimalType.
        commandDispatcher.on(CHANNEL_COLOR_TEMPERATURE, DecimalType.class, command -> {
            JsonColorLed dev = new JsonColorLed(deviceDescriptor);
            dev.getDps().setDp4(numberTo255(command));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.OFF);
            return dev;
        });

        // Color with HSBType.
        commandDispatcher.on(CHANNEL_COLOR, HSBType.class, command -> {
            JsonColorLed dev = new JsonColorLed(deviceDescriptor);
            dev.getDps().setDp5(colorToCommandString((HSBType) command));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.ON);
            return dev;
        });
    }

}
