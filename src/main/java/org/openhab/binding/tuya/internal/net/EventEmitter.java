/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Generic Event Emitter.
 *
 * @author Wim Vissers.
 *
 * @param &lt;E&gt; the event type.
 * @param &lt;P&gt; the type of payload the consumer accepts.
 */
public class EventEmitter<E, P> {

    /**
     * Key: the event name. Value: a list of consumer wrappers to call when the event
     * happens.
     */
    private final ConcurrentHashMap<E, List<ConsumerWrapper>> eventConsumers;

    public EventEmitter() {
        eventConsumers = new ConcurrentHashMap<>();
    }

    /**
     * To be overridden by subclasses to handle initial events for new
     * listeners.
     */
    protected void handlerAdded(E event, Consumer<P> eventConsumer, Consumer<Exception> exceptionConsumer) {
    }

    /**
     * Register a consumer to listen for certain events.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    private synchronized EventEmitter<E, P> onEvent(E event, Consumer<P> eventConsumer,
            Consumer<Exception> exceptionConsumer, boolean once, boolean first) {
        List<ConsumerWrapper> consumerList = eventConsumers.get(event);
        if (consumerList == null) {
            consumerList = new ArrayList<>();
            eventConsumers.put(event, consumerList);
        }
        if (first) {
            consumerList.add(0, new ConsumerWrapper(eventConsumer, exceptionConsumer, once));
        } else {
            consumerList.add(new ConsumerWrapper(eventConsumer, exceptionConsumer, once));
        }
        handlerAdded(event, eventConsumer, exceptionConsumer);
        return this;
    }

    /**
     * Register a consumer to listen for certain events.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    public EventEmitter<E, P> on(E event, Consumer<P> consumer) {
        return onEvent(event, consumer, null, false, false);
    }

    /**
     * Register a consumer to listen for certain events. Put it first in the list of
     * consumers to call.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    public EventEmitter<E, P> onFirst(E event, Consumer<P> consumer) {
        return onEvent(event, consumer, null, false, true);
    }

    /**
     * Register a consumer to listen for certain events. Only executed once.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    public EventEmitter<E, P> once(E event, Consumer<P> consumer) {
        return onEvent(event, consumer, null, true, false);
    }

    /**
     * Register a consumer to listen for certain events. Only executed once. Put it
     * first in the list of consumers to call.
     *
     * @param eventName the name of the event to listen to.
     * @param consumer  the consumer to call.
     */
    public EventEmitter<E, P> onceFirst(E event, Consumer<P> consumer) {
        return onEvent(event, consumer, null, true, true);
    }

    /**
     *
     * @param event
     * @return
     */
    public synchronized EventEmitter<E, P> removeAllConsumers(E event) {
        eventConsumers.remove(event);
        return this;
    }

    /**
     * Emit an event with name eventName and payload.
     *
     * @param eventName the event name.
     * @param payload   the payload of type P.
     */
    public void emit(E event, P payload) {
        List<ConsumerWrapper> consumerList = eventConsumers.get(event);
        if (consumerList != null) {
            consumerList.forEach(consumer -> {
                consumer.accept(payload);
            });
        }
    }

    public void stop() {
    }

    /**
     *
     * @author Wim Vissers
     *
     */
    private class ConsumerWrapper {

        private final Consumer<P> eventConsumer;
        private final Consumer<Exception> exceptionConsumer;
        private final boolean once;
        private boolean executed;

        public ConsumerWrapper(Consumer<P> eventConsumer, Consumer<Exception> exceptionConsumer, boolean once) {
            this.eventConsumer = eventConsumer;
            this.exceptionConsumer = exceptionConsumer;
            this.once = once;
        }

        public void accept(P payload) {
            if (!(once & executed)) {
                try {
                    eventConsumer.accept(payload);
                } catch (Exception ex) {
                    if (exceptionConsumer == null) {
                        throw ex;
                    } else {
                        exceptionConsumer.accept(ex);
                    }
                }
            }
            executed = true;
        }
    }

}
