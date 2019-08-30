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
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.tuya.internal.util.SingleEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for UDP messages on the specified port.
 *
 * @author Wim Vissers.
 *
 */
public class DatagramListener extends SingleEventEmitter<DatagramListener.Event, ByteBuffer, Boolean>
        implements UdpConfig {

    private Future<?> task;
    private int port;
    private final Logger logger;
    private boolean running;

    public DatagramListener(int port) {
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
                    DatagramChannel channel = null;
                    ByteBuffer buf = ByteBuffer.allocate(UDP_SOCKET_BUFFER_SIZE);
                    while (running) {
                        try {
                            if (channel == null) {
                                channel = DatagramChannel.open();
                                channel.socket().bind(new InetSocketAddress(port));
                            }
                            buf.clear();
                            channel.receive(buf);
                            byte[] res = new byte[buf.position()];
                            buf.flip();
                            buf.get(res);
                            emit(Event.UDP_PACKET_RECEIVED, buf);
                        } catch (SocketTimeoutException ignored) {
                        } catch (IOException ex1) {
                            logger.error("DatagramEventEmitter", ex1);
                        } finally {
                            if (channel != null) {
                                try {
                                    channel.close();
                                    channel = null;
                                } catch (IOException e) {
                                }
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
