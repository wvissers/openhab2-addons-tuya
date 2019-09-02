/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal;

import java.io.IOException;
import java.util.function.BiFunction;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.CommandDispatcher.CommandEvent;
import org.openhab.binding.tuya.internal.data.CommandByte;
import org.openhab.binding.tuya.internal.data.DeviceState;
import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.net.TuyaClient;
import org.openhab.binding.tuya.internal.util.SingleEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic handler of openHAB commands. In particular, it dispatches commands to the right handlers for processing.
 *
 * &lt;D&gt; Datatype for dispatching.
 *
 * @author Wim Vissers.
 *
 */
public class CommandDispatcher extends SingleEventEmitter<CommandEvent, Command, DeviceState> {

    private Logger logger = LoggerFactory.getLogger(CommandDispatcher.class);
    private final ThingUID thingUID;

    public CommandDispatcher(ThingUID thingUID) {
        // eventCallbacks = new ConcurrentHashMap<>();
        this.thingUID = thingUID;
    }

    /**
     * Add an event handler to the map.
     *
     * @param channelUID   the channel UID.
     * @param commandClass the command class.
     * @param callback     the callback function that must return a DeviceState object to transmit, or null to avoid
     *                         sending.
     * @return this CommandHandler.
     */
    public CommandDispatcher on(String channel, Class<?> commandClass,
            BiFunction<CommandEvent, Command, DeviceState> callback) {
        CommandEvent event = new CommandEvent(new ChannelUID(thingUID, channel), commandClass);
        on(event, callback);
        return this;
    }

    /**
     * Dispatch a single command.
     *
     * @param client      the client object.
     * @param channelUID  the channel uid this client received in its handleCommand call.
     * @param command     the command this client received in its handleCommand call.
     * @param commandByte the Tuya commandbyte that will be used to construct the mesage to teh Tuya device.
     * @return true when the command is handled, otherwise false.
     */
    public boolean dispatchCommand(TuyaClient client, ChannelUID channelUID, Command command, CommandByte commandByte) {
        CommandEvent event = new CommandEvent(channelUID, command.getClass());
        DeviceState data = emit(event, command);
        if (data != null) {
            try {
                client.send(data, commandByte);
                event.setHandled(true);
            } catch (IOException | ParseException e) {
                logger.error("Error dispatching command.", e);
            }
        }
        return event.isHandled();
    }

    /**
     * The wrapper for the command and the channel the command applies to. The CommandHandler can be used to add
     * handlers for combinations of ChannelUID and Command class. This class is only used internally in this command
     * handler. It overrides the hashCode and equals methods, in order to use it as a key in a Map.
     *
     * @author Wim Vissers.
     *
     */
    public class CommandEvent {

        private final ChannelUID channelUID;
        private final Class<?> commandClass;
        private boolean handled;

        public CommandEvent(ChannelUID channelUID, Class<?> commandClass) {
            this.channelUID = channelUID;
            this.commandClass = commandClass;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((channelUID == null) ? 0 : channelUID.hashCode());
            result = prime * result + ((commandClass == null) ? 0 : commandClass.toString().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CommandEvent other = (CommandEvent) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (channelUID == null) {
                if (other.channelUID != null) {
                    return false;
                }
            } else if (!channelUID.equals(other.channelUID)) {
                return false;
            }
            if (commandClass == null) {
                if (other.commandClass != null) {
                    return false;
                }
            } else if (!commandClass.equals(other.commandClass)) {
                return false;
            }
            return true;
        }

        public boolean isHandled() {
            return handled;
        }

        public void setHandled(boolean handled) {
            this.handled = handled;
        }

        private CommandDispatcher getOuterType() {
            return CommandDispatcher.this;
        }

    }
}
