/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import static java.lang.System.arraycopy;
import static java.nio.channels.SelectionKey.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.tuya.internal.exceptions.NoDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton is responsible for creating and servicing TCP client for Tuya devices. It has a factory method to
 * create new clients.
 *
 * @author Wim Vissers.
 *
 */
public class TuyaClientService implements Runnable, TcpConfig {

    // Create and store the singleton implementation.
    private static final TuyaClientService INSTANCE = new TuyaClientService();

    private final ExecutorService executor;
    private Selector selector;

    // Service status.
    private boolean running;

    // The main task Future.
    private Future<?> mainTask;

    // Buffer to receive data.
    private ByteBuffer buffer = ByteBuffer.allocate(TCP_SOCKET_BUFFER_SIZE);

    // Table containing the clients. The Selection keys attachment are not suitable.
    private ConcurrentHashMap<SelectionKey, TuyaClient> clients = new ConcurrentHashMap<>();

    private final Logger logger;

    /**
     * Private constructor to ensure singleton.
     */
    public TuyaClientService() {
        logger = LoggerFactory.getLogger(this.getClass());
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get the singleton instance.
     *
     * @return the instance.
     */
    public static TuyaClientService getInstance() {
        return INSTANCE;
    }

    /**
     * Create a SelectionKey for the given client, and register the client to be serviced.
     *
     * @param client the TuyaClient.
     * @param host   the host (name or ip address).
     * @param port   the TCP port.
     * @return the SelectionKey for this client.
     * @throws IOException when something goes wrong.
     */
    public SelectionKey register(TuyaClient client, String host, int port) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(host, port < 0 ? DEFAULT_SERVER_PORT : port));
        SelectionKey key = channel.register(selector, OP_CONNECT);
        clients.put(key, client);
        start();
        return key;
    }

    /**
     * Remove keys that are cancelled.
     */
    private void cleanClientsMap() {
        clients.keySet().forEach(key -> {
            if (!key.isValid()) {
                try {
                    key.channel().close();
                } catch (IOException ignored) {
                }
                clients.remove(key);
            }
        });
    }

    /**
     * The main loop services incoming data for all created clients.
     */
    @Override
    public void run() {
        while (running) {
            try {
                selector.select(SELECTOR_TIMEOUT_MILLIS);

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        connect(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                }
                cleanClientsMap();
            } catch (IOException e) {
                logger.error("IOException servicing Tuya client", e);
            }

        }
        cleanUp();
    }

    /**
     * Handle connect request.
     *
     * @param key the SelectionKey.
     * @throws IOException
     */
    protected void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        TuyaClient client = clients.get(key);
        try {
            logger.debug("Connecting {}.", client);
            channel.finishConnect();
            channel.configureBlocking(false);
            channel.register(selector, OP_WRITE);
            client.handleConnect(key);
        } catch (IOException e) {
            logger.debug("Error connecting {}.", client);
            key.channel().close();
            key.cancel();
            if (client != null) {
                client.handleDisconnect(key, e);
            }
        }
    }

    /**
     * Handle read request.
     *
     * @param key the SelectionKey.
     * @throws IOException
     */
    protected void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        TuyaClient client = clients.get(key);
        if (client == null) {
            logger.error("No client for key {}.", key);
        }

        logger.debug("Read from channel {}.", channel);
        int readCount;
        buffer.clear();

        try {
            readCount = channel.read(buffer);
        } catch (IOException e) {
            key.cancel();
            channel.close();
            if (client != null) {
                client.handleDisconnect(key, e);
            }
            return;
        }

        if (readCount == -1) {
            // Channel is no longer active - clean up
            key.channel().close();
            key.cancel();
            if (client != null) {
                client.handleDisconnect(key, new NoDataException());
            }
            return;

        }

        if (client != null) {
            byte[] data = new byte[buffer.position()];
            arraycopy(buffer.array(), 0, data, 0, buffer.position());
            client.handleData(key, data);
        }
    }

    /**
     * Handle write request.
     *
     * @param key the SelectionKey.
     * @throws IOException
     */
    protected void write(SelectionKey key) throws IOException {
        logger.debug("Write to channel {}.", key.channel());
        TuyaClient client = clients.get(key);
        if (client == null) {
            logger.error("Missing client for key {}", key);
        } else {
            client.writeData(key);
        }
    }

    /**
     * Free used resources when possible.
     */
    private void cleanUp() {
        for (SelectionKey key : selector.keys()) {
            this.close(key.channel());
        }
        close(selector);
        if (mainTask != null) {
            mainTask.cancel(true);
            mainTask = null;
        }
    }

    /**
     * Start the main loop.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        if (!running || (mainTask != null && (mainTask.isCancelled() || mainTask.isDone()))) {
            running = true;
            selector = Selector.open();
            mainTask = executor.submit(this);
        }
    }

    /**
     * Stop running.
     */
    public void stop() {
        if (running) {
            running = false;
            executor.shutdown();
            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

}
