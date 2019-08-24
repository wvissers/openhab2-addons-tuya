/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for UDP messages on the specified port.
 *
 * @author Wim Vissers.
 *
 */
public class DatagramEventEmitter extends EventEmitter<DatagramEventEmitter.Event, Packet> {

    private Future<?> task;
    private int port;
    private final Logger logger;
    private boolean running;

    public DatagramEventEmitter(int port) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.port = port;
    }

    /**
     * Main loop.
     */
    public void start(ScheduledExecutorService scheduler) {
        if (task == null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    running = true;
                    DatagramSocket listener = null;
                    while (running) {
                        byte[] result = new byte[1024];
                        try {
                            listener = new DatagramSocket(port);
                            DatagramPacket dp = new DatagramPacket(result, 1024);
                            listener.setSoTimeout(120000);
                            listener.receive(dp);
                            emit(Event.UDP_PACKET_RECEIVED, new Packet(result, dp.getLength()));
                            listener.close();
                        } catch (IOException ex1) {
                            logger.error("DatagramEventEmitter", ex1);
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                            }
                        } finally {
                            if (listener != null) {
                                listener.close();
                            }
                        }
                    }
                }
            };
            task = scheduler.submit(runnable);
        }
    }

    @Override
    public void stop() {
        running = false;
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        super.stop();
    }

    public enum Event {
        UDP_PACKET_RECEIVED;
    }

}
