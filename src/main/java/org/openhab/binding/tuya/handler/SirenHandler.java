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

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.tuya.internal.data.Message;
import org.openhab.binding.tuya.internal.data.SirenState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler for a Tuya Switch device.
 *
 * @author Wim Vissers
 *
 */
public class SirenHandler extends AbstractTuyaHandler {

    private Logger logger = LoggerFactory.getLogger(SirenHandler.class);

    // The duration 1..30 seconds is handled by the binding, not by the siren itself.
    private int duration;

    public SirenHandler(Thing thing) {
        super(thing);
        duration = 10;
    }

    /**
     * This method is called when a DeviceEventEmitter.Event.MESSAGE_RECEIVED is received from the device. This could
     * result in a possible state change of this things channels.
     */
    @Override
    protected void handleStatusMessage(Message message) {
        updateStates(message, SirenState.class);
    }

    /**
     * Add the commands to the dispatcher.
     */
    @Override
    protected void initCommandDispatcher() {

        // Channel alarm command with OnOffType.
        commandDispatcher.on(CHANNEL_ALARM, OnOffType.class, (ev, command) -> {
            // Schedule the timeout
            if (command.equals(OnOffType.ON)) {
                scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ALARM), OnOffType.OFF);
                    }
                }, duration, TimeUnit.SECONDS);
            }
            return new SirenState(deviceDescriptor).withAlarm(command);
        });

        // Channel volume command with DecimalType.
        commandDispatcher.on(CHANNEL_VOLUME, DecimalType.class, (ev, command) -> {
            return new SirenState(deviceDescriptor).withVolume(command);
        });

        // Channel duration command with DecimalType.
        commandDispatcher.on(CHANNEL_DURATION, DecimalType.class, (ev, command) -> {
            duration = ((DecimalType) command).intValue();
            return new SirenState(deviceDescriptor).withDuration(command);
        });
    }

}
