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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.tuya.internal.json.JsonFilamentLed;
import org.openhab.binding.tuya.internal.net.Message;

/**
 * A handler for a Tuya Filament LED device.
 *
 * @author Wim Vissers
 *
 */
public class FilamentLedHandler extends AbstractTuyaHandler {

    public FilamentLedHandler(Thing thing) {
        super(thing);
    }

    /**
     * This method is called when a DeviceEventEmitter.Event.MESSAGE_RECEIVED is received from the device. This could
     * result in a possible state change of this things channels.
     */
    @Override
    protected void handleStatusMessage(Message message) {
        if (message != null) {
            JsonFilamentLed dev = message.toFilamentLed();
            if (dev != null) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_POWER),
                        dev.getDps().isDp1() ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    /**
     * Add the commands to the dispatcher.
     */
    @Override
    protected void initCommandDispatcher() {
        // Channel power command with OnOffType.
        commandDispatcher.on(CHANNEL_POWER, OnOffType.class, command -> {
            JsonFilamentLed dev = new JsonFilamentLed(deviceDescriptor);
            dev.getDps().setDp1(command == OnOffType.ON);
            return dev;
        });

        // Brightness with DecimalType.
        commandDispatcher.on(CHANNEL_BRIGHTNESS, DecimalType.class, command -> {
            JsonFilamentLed dev = new JsonFilamentLed(deviceDescriptor);
            dev.getDps().setDp2(numberTo255(command));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.OFF);
            return dev;
        });

        // Color temperature with DecimalType.
        commandDispatcher.on(CHANNEL_COLOR_TEMPERATURE, DecimalType.class, command -> {
            JsonFilamentLed dev = new JsonFilamentLed(deviceDescriptor);
            dev.getDps().setDp3(numberTo255(command));
            updateState(new ChannelUID(thing.getUID(), CHANNEL_COLOR_MODE), OnOffType.OFF);
            return dev;
        });
    }

}
