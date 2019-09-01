/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import static java.nio.channels.SelectionKey.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.tuya.internal.data.CommandByte;
import org.openhab.binding.tuya.internal.data.DeviceState;
import org.openhab.binding.tuya.internal.data.Message;
import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.exceptions.UnsupportedVersionException;
import org.openhab.binding.tuya.internal.util.MessageParser;
import org.openhab.binding.tuya.internal.util.SingleEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TuyaClient is a TCP client implementation for communicating with a single device. Please use the factory method in
 * TuyaClientService to obtain a client. It will be automatically registered with the service and serviced.
 *
 * @author Wim Vissers.
 *
 */
public class TuyaClient extends SingleEventEmitter<TuyaClient.Event, Message, Boolean> implements TcpConfig {

    // The message parser is to encode/decode messages. It is dedicated to a
    // single device, since the localKey is different from device to device.
    private MessageParser messageParser;

    // The sequence number of messages sent to the device.
    private long currentSequenceNo;

    // The queue for outgoing messages.
    private final LinkedBlockingQueue<byte[]> queue;

    // The selection key.
    private SelectionKey key;

    // The heartbeat task.
    private ScheduledFuture<?> heartbeat;

    // Count heartbeats that have not been acknowledged yet.
    private final AtomicInteger heartbeatCnt;

    // Host and port
    private String host;
    private int port;

    private boolean online;
    private final Logger logger;

    /**
     * Create a new TuyaClient with the given parameters.
     *
     * @param selector the Selector servicing this client.
     * @param host     the Tuya host ip-address or name.
     * @param port     the port number. When -1, the default port number is used.
     * @param version  the Tuya API version.
     * @param localKey the localKey for encryption of messages.
     * @throws UnsupportedVersionException
     */
    public TuyaClient(String host, int port, String version, String localKey) throws UnsupportedVersionException {
        if (!version.equals(DEFAULT_VERSION)) {
            throw new UnsupportedVersionException("Currently only version 3.3. supported");
        }

        // Create a message parser for the given version and localKey.
        messageParser = new MessageParser(version, localKey);
        logger = LoggerFactory.getLogger(this.getClass());
        this.queue = new LinkedBlockingQueue<>(DEFAULT_QUEUE_SIZE);
        this.host = host;
        this.port = port < 0 ? DEFAULT_SERVER_PORT : port;
        heartbeatCnt = new AtomicInteger();
    }

    /**
     * Start this client. It will be registered to the TuyaClientService. The scheduler will be used for repetitive or
     * short running tasks.
     *
     * @param scheduler the scheduler.
     */
    public void start(ScheduledExecutorService scheduler) {
        try {
            connect();
            if (heartbeat == null) {
                heartbeat = scheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            send("", CommandByte.HEART_BEAT);
                        } catch (IOException | ParseException e) {
                        }
                    }
                }, HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            emit(Event.CONNECTION_ERROR, null);
        }
    }

    /**
     * Stop the client.
     */
    @Override
    public void stop() {
        online = false;
        if (heartbeat != null) {
            heartbeat.cancel(false);
            heartbeat = null;
        }
        super.stop();
    }

    /**
     * Connect the client and register to the client service.
     *
     * @throws IOException
     */
    public void connect() throws IOException {
        key = TuyaClientService.getInstance().register(this, host, port);
        heartbeatCnt.set(0);
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
        if (!online || key == null) {
            connect();
        }
        if (queue.remainingCapacity() == 0) {
            if (online) {
                online = false;
                emit(Event.CONNECTION_ERROR, null);
            }
        } else {
            byte[] packet = messageParser.encode(message.getBytes(), command, currentSequenceNo++);
            queue.offer(packet);
            if (command.equals(CommandByte.HEART_BEAT)) {
                if (heartbeatCnt.incrementAndGet() > HEARTBEAT_RETRIES) {
                    online = false;
                    emit(Event.CONNECTION_ERROR, new Message("no response to heartbeat"));
                }
            }
            key.interestOps(OP_WRITE);
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
    public void send(DeviceState device, CommandByte command) throws IOException, ParseException {
        send(device.toJson(), command);
    }

    /**
     * Called by the service when connected.
     *
     * @param key the selection key.
     */
    void handleConnect(SelectionKey key) {
        this.key = key;
        online = true;
        emit(Event.CONNECTED, null);
    }

    /**
     * Called by the service when disconnected.
     *
     * @param key the selection key.
     * @param ex  the IOException (may by null).
     */
    void handleDisconnect(SelectionKey key, IOException ex) {
        this.key = null;
        online = false;
        if (ex == null) {
            emit(Event.DISCONNECTED, null);
        } else {
            emit(Event.CONNECTION_ERROR, null);
        }
    }

    /**
     * Called by the service when data arrived.
     *
     * @param key  the selection key.
     * @param data the raw data bytes.
     */
    void handleData(SelectionKey key, byte[] data) {
        logger.debug("Incoming message from {} with data {}", key, data);
        try {
            Message message = messageParser.decode(data);
            if (message.getCommandByte().equals(CommandByte.HEART_BEAT)) {
                if (heartbeatCnt.intValue() > 0) {
                    heartbeatCnt.decrementAndGet();
                }
            }
            emit(Event.MESSAGE_RECEIVED, message);
        } catch (ParseException e) {
            logger.error("Invalid message received.");
        }
        if (!queue.isEmpty() && key != null) {
            key.interestOps(OP_WRITE);
        }
    }

    /**
     * Called by the service when ready for writing.
     *
     * @param key the selection key.
     */
    void writeData(SelectionKey key) {
        logger.debug("Write data requested.");
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (!queue.isEmpty()) {
                channel.write(ByteBuffer.wrap(queue.poll()));
            }
        } catch (IOException e) {
            return;
        }
        key.interestOps(OP_READ);
    }

    public enum Event {
        CONNECTION_ERROR,
        CONNECTED,
        DISCONNECTED,
        MESSAGE_RECEIVED;
    }

}
