/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import java.io.Closeable;
import java.io.IOException;

/**
 * Setting for TCP clients.
 *
 * @author Wim Vissers.
 *
 */
public interface TcpConfig {

    // Settings for the TCP client (the device is the server).
    public static final int SELECTOR_TIMEOUT_MILLIS = 3000;
    public static final int INIT_DELAY_SECONDS = 2;
    public static final int DEFAULT_SERVER_PORT = 6668;
    public static final String DEFAULT_VERSION = "3.3";
    public static final int TCP_SOCKET_BUFFER_SIZE = 1024;
    public static final int MAX_RETRIES = 3;
    public static final int HEARTBEAT_SECONDS = 15;
    public static final int HEARTBEAT_RETRIES = 3;
    public static final int OUTSTANDING_HEARTBEATS_LIMIT = 3;
    public static final int WATCHDOG_CHECK_SECONDS = 30;
    public static final int DEFAULT_QUEUE_SIZE = 20;
    public static final int STATUS_REQUEST_DELAY_SECONDS = 120;

    /**
     * Default method to close used resources and silently ignoring IOExceptions if they occur.
     *
     * @param closeables
     */
    public default void close(Closeable... closeables) {
        try {
            for (Closeable closeable : closeables) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }
    }

}
