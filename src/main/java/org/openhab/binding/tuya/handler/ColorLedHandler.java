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
 * A handler for a Tuya Color LED device.
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
        if (message != null) {
            JsonColorLed dev = message.toColorLed();
            if (dev != null) {
                if (dev.getPower() != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_POWER), dev.getPower());
                }
            }
        }
    }

    /**
     * Add the commands to the dispatcher.
     */
    @Override
    protected void initCommandDispatcher() {
        // Channel power command with OnOffType.
        commandDispatcher.on(CHANNEL_POWER, OnOffType.class, (ev, command) -> {
            return new JsonColorLed(deviceDescriptor).withPower(command);
        });

        // Color mode command with OnOffType.
        commandDispatcher.on(CHANNEL_COLOR_MODE, OnOffType.class, (ev, command) -> {
            return new JsonColorLed(deviceDescriptor).withColorMode(command);
        });

        // Brightness with DecimalType.
        commandDispatcher.on(CHANNEL_BRIGHTNESS, DecimalType.class, (ev, command) -> {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.OFF);
            return new JsonColorLed(deviceDescriptor).withBrightness(command).withColorMode(OnOffType.OFF);
        });

        // Color temperature with DecimalType.
        commandDispatcher.on(CHANNEL_COLOR_TEMPERATURE, DecimalType.class, (ev, command) -> {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.OFF);
            return new JsonColorLed(deviceDescriptor).withColorTemperature(command).withColorMode(OnOffType.OFF);
        });

        // Color with HSBType.
        commandDispatcher.on(CHANNEL_COLOR, HSBType.class, (ev, command) -> {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.ON);
            return new JsonColorLed(deviceDescriptor).withColor(command).withColorMode(OnOffType.ON);
        });
    }

}
