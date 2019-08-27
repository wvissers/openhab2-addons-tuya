/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import static org.openhab.binding.tuya.TuyaBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.tuya.internal.json.CommandByte;
import org.openhab.binding.tuya.internal.json.JsonData;
import org.openhab.binding.tuya.internal.util.MessageParser;
import org.openhab.binding.tuya.internal.util.ParseException;
import org.openhab.binding.tuya.internal.util.SingletonEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This emitter is used for a connection with a device, and communicating
 * in both directions.
 *
 * @author Wim Vissers.
 *
 */
public class DeviceEventEmitter extends SingletonEventEmitter<DeviceEventEmitter.Event, Message, Boolean> {

    private Future<?> task;
    ScheduledFuture<?> heartbeat;
    private final Logger logger;
    private final String host;
    private final int port;
    private final MessageParser parser;
    private Socket clientSocket;
    private long currentSequenceNo;
    private boolean running;
    private boolean online;
    private final LinkedBlockingQueue<byte[]> queue;
    private static final Gson gson = new Gson();
    // private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Create a device event emitter. Use connect() after creation to establish a connection.
     *
     * @param host the ip address or host name.
     * @param port the port number to connect to.
     */
    public DeviceEventEmitter(String host, int port, MessageParser parser) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.host = host;
        this.port = port;
        this.parser = parser;
        this.queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    }

    /**
     * Send a message. If the device responds, the response will be emitted as a new event.
     *
     * @param message the message to send as string.
     * @param command the commandbyte enum constant.
     * @throws IOException
     * @throws ParseException
     */
    public void send(String message, CommandByte command) throws IOException, ParseException {
        if (queue.remainingCapacity() == 0) {
            if (online) {
                online = false;
                emit(Event.CONNECTION_ERROR, null);
            }
        } else {
            byte[] packet = parser.encode(message.getBytes(), command, currentSequenceNo++);
            queue.offer(packet);
        }
    }

    /**
     * Send a message. If the device responds, the response will be emitted as a new event.
     *
     * @param device  the device object that will be transformed to a json string.
     * @param command the commandbyte enum constant.
     * @throws IOException
     * @throws ParseException
     */
    public void send(JsonData device, CommandByte command) throws IOException, ParseException {
        send(gson.toJson(device), command);
    }

    /**
     * Create a client socket and setup the connection.
     */
    private void connect() {
        try {
            clientSocket = new Socket();
            clientSocket.setReuseAddress(true);
            clientSocket.connect(new InetSocketAddress(host, port));
            if (clientSocket.isConnected()) {
                online = true;
                emit(Event.CONNECTED, null);
            }
        } catch (IOException e) {
            emit(Event.CONNECTION_ERROR, new Message(e.getMessage()));
        }
    }

    /**
     * Disconnect the client socket and emit Event.DISCONNECTED when successful, or Event.CONNECTION_ERROR when it
     * fails.
     */
    private void disconnect() {
        online = false;
        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientSocket = null;
                emit(Event.DISCONNECTED, null);
            } catch (IOException e) {
                emit(Event.CONNECTION_ERROR, new Message(e.getMessage()));
            }
        }
    }

    @Override
    public void stop() {
        online = false;
        running = false;
        disconnect();
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        if (heartbeat != null) {
            heartbeat.cancel(false);
            heartbeat = null;
        }
        super.stop();
    }

    /**
     * Create the main task as runnable that can be executed by the scheduler.
     *
     * @return a new runnable task.
     */
    private Runnable createMainTask() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                OutputStream out = null;
                InputStream in = null;
                try {
                    running = true;
                    int retries = 0;
                    byte[] buffer = new byte[TCP_SOCKET_BUFFER_SIZE];
                    while (running) {
                        try {
                            if (clientSocket == null || !clientSocket.isConnected()) {
                                connect();
                                out = clientSocket.getOutputStream();
                                in = clientSocket.getInputStream();
                            }
                            if (out != null && !queue.isEmpty()) {
                                retries++;
                                byte[] packet = queue.peek();
                                out.write(packet);
                                out.flush();
                                queue.poll();
                                retries--;
                            }
                            Thread.sleep(200);
                            if (in != null && in.available() > 10) {
                                Thread.sleep(20);
                                int len = in.read(buffer, 0, TCP_SOCKET_BUFFER_SIZE);
                                List<Message> res = parser.parse(buffer, len);
                                for (Message msg : res) {
                                    emit(Event.MESSAGE_RECEIVED, msg);
                                }
                            }
                        } catch (IOException | InterruptedException e) {
                            emit(Event.CONNECTION_ERROR, new Message(e.getMessage()));
                            disconnect();
                            try {
                                if (retries > MAX_RETRIES) {
                                    queue.poll();
                                    retries = 0;
                                    Thread.sleep(30000);
                                } else {
                                    Thread.sleep(500);
                                }
                            } catch (InterruptedException ex) {
                            }
                        } catch (ParseException e) {
                            logger.error("Invalid message received.");
                        }
                    }
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException | NullPointerException e) {
                    }
                    // stop();
                }
            }
        };
        return runnable;
    }

    /**
     * Create heartbeat task.
     *
     * @return
     */
    private Runnable createHeartBeat() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    send("", CommandByte.HEART_BEAT);
                } catch (IOException | ParseException e) {
                    // Should not happen
                }
            }
        };
        return runnable;

    }

    /**
     * Start this as a task in the executor service. For some reason the scheduler doesn't work here. Need to look into
     * that some time.
     *
     * @param scheduler
     */
    public void start(ScheduledExecutorService scheduler, boolean keepAlive) {
        if (task == null) {
            task = scheduler.submit(createMainTask());
        }
        if (heartbeat == null && keepAlive) {
            heartbeat = scheduler.scheduleAtFixedRate(createHeartBeat(), 0, HEARTBEAT_SECONDS, TimeUnit.SECONDS);
        }
    }

    public enum Event {
        CONNECTION_ERROR,
        CONNECTED,
        DISCONNECTED,
        MESSAGE_RECEIVED;
    }

}
