/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal;

import java.util.function.Function;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tuya.internal.CommandHandler.CommandEvent;
import org.openhab.binding.tuya.internal.net.EventEmitter;

/**
 * Generic handler of openHAB commands. In particular, it dispatches commands to the right handlers for processing.
 *
 * @author Wim Vissers.
 *
 */
public class CommandHandler extends EventEmitter<CommandEvent, Function<Command, State>> {

    public CommandHandler() {
    }

    protected class CommandEvent {
        private ChannelUID channelUID;
        private Command command;
    }
}
