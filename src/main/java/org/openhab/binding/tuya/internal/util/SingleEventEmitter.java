/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * An event emitter that only allows a single handler for each event.
 *
 * @author Wim Vissers.
 *
 * @param &lt;E&gt; the event type.
 * @param &lt;P&gt; the type of payload the consumer accepts.
 * @param &lt;R&gt; the type of return object the callback functions provide.
 */
public class SingleEventEmitter<E, P, R> {

    /**
     * Key: the event name. Value: a BiFunction to call when the event happens.
     */
    private final ConcurrentHashMap<E, BiFunction<E, P, R>> callbacks;

    public SingleEventEmitter() {
        callbacks = new ConcurrentHashMap<>();
    }

    /**
     * Register a consumer to listen for certain events.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    public synchronized SingleEventEmitter<E, P, R> on(E event, BiFunction<E, P, R> callback) {
        if (event != null) {
            if (callbacks.containsKey(event)) {
                throw new IllegalArgumentException("Cannot add more than one callback for an event.");
            }
            callbacks.put(event, callback);
            handlerAdded(event, callback);
        }
        return this;
    }

    /**
     * Register a consumer to listen for multiple events.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    @SafeVarargs
    public final SingleEventEmitter<E, P, R> handle(BiFunction<E, P, R> callback, E... events) {
        for (E event : events) {
            on(event, callback);
        }
        return this;
    }

    /**
     * Remove the handler associated with the given event.
     *
     * @param event
     * @return
     */
    public SingleEventEmitter<E, P, R> removeHandler(E event) {
        callbacks.remove(event);
        return this;
    }

    public SingleEventEmitter<E, P, R> removeAllHandlers() {
        callbacks.clear();
        return this;
    }

    /**
     * Emit an event with name eventName and payload. Return the first non-null
     * value the callback functions return, or null if none of them returned a
     * non-null value.
     *
     * @param eventName the event name.
     * @param payload   the payload of type P.
     */
    public R emit(E event, P payload) {
        if (callbacks.containsKey(event)) {
            return callbacks.get(event).apply(event, payload);
        }
        return null;
    }

    /**
     * Override in subclasses to stop running tasks.
     */
    public void stop() {
    }

    /**
     * When a new handler is added, this method is called. Override when necessary.
     */
    protected void handlerAdded(E event, BiFunction<E, P, R> callback) {

    }

}
