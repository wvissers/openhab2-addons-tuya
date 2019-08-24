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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.json.CommandByte;
import org.openhab.binding.tuya.internal.json.JsonData;
import org.openhab.binding.tuya.internal.net.DeviceEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic handler of openHAB commands. In particular, it dispatches commands to the right handlers for processing.
 *
 * @author Wim Vissers.
 *
 */
public class CommandDispatcher {

    /**
     * Key: the event key. Value: a list of consumer wrappers to call when the event
     * happens.
     */
    private final ConcurrentHashMap<String, List<Function<Command, JsonData>>> eventCallbacks;
    private Logger logger = LoggerFactory.getLogger(CommandDispatcher.class);
    private final ThingUID thingUID;

    public CommandDispatcher(ThingUID thingUID) {
        eventCallbacks = new ConcurrentHashMap<>();
        this.thingUID = thingUID;
    }

    /**
     * Add an event handler to the map.
     *
     * @param channelUID   the channel UID.
     * @param commandClass the command class.
     * @param callback     the callback function that must return a JsonData object to transmit, or null to avoid
     *                         sending.
     * @return this CommandHandler.
     */
    public synchronized CommandDispatcher on(String channel, Class<?> commandClass,
            Function<Command, JsonData> callback) {
        CommandEvent event = new CommandEvent(new ChannelUID(thingUID, channel), commandClass);
        List<Function<Command, JsonData>> callbackList = eventCallbacks.get(event.getKey());
        if (callbackList == null) {
            callbackList = new ArrayList<>();
            eventCallbacks.put(event.getKey(), callbackList);
        }
        callbackList.add(callback);
        return this;
    }

    public void dispatchCommand(DeviceEventEmitter emitter, ChannelUID channelUID, Command command,
            CommandByte commandByte) {
        CommandEvent event = new CommandEvent(channelUID, command.getClass());
        List<Function<Command, JsonData>> callbackList = eventCallbacks.get(event.getKey());
        if (callbackList != null) {
            callbackList.forEach(callback -> {
                JsonData data = callback.apply(command);
                if (data != null) {
                    try {
                        emitter.send(data, commandByte);
                    } catch (IOException | ParseException e) {
                        logger.error("Error dispatching command.", e);
                    }
                }
            });
        }
    }

    public void dispatchCommand(DeviceEventEmitter emitter, ChannelUID channelUID, Command command) {
        dispatchCommand(emitter, channelUID, command, CommandByte.CONTROL);
    }

    /**
     * The wrapper for the command and the channel the command applies to. The CommandHandler can be used to add
     * handlers for combinations of ChannelUID and Command class. This class is only used internally in this command
     * handler.
     *
     * @author Wim Vissers.
     *
     */
    protected class CommandEvent {

        private final ChannelUID channelUID;
        private final Class<?> commandClass;

        public CommandEvent(ChannelUID channelUID, Class<?> commandClass) {
            this.channelUID = channelUID;
            this.commandClass = commandClass;
        }

        /**
         * Construct a unique key for the event.
         *
         * @return
         */
        public String getKey() {
            return channelUID.getAsString() + ":" + commandClass.getName();
        }
    }
}
