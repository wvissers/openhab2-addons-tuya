/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

/**
 * Setting for TCP clients.
 *
 * @author Wim Vissers.
 *
 */
public interface TcpSettings {

    // Settings for the TCP client (the device is the server).
    public static final int DEFAULT_SERVER_PORT = 6668;
    public static final int TCP_SOCKET_BUFFER_SIZE = 1024;
    public static final int MAX_RETRIES = 5;
    public static final int HEARTBEAT_SECONDS = 10;
    public static final int QUEUE_SIZE = 3;

}
