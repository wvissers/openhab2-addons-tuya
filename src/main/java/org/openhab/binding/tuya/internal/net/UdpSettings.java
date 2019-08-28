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
 * Setting for UDP listener.
 *
 * @author Wim Vissers.
 *
 */
public interface UdpSettings {

    // Setting for UDP broadcast listener (currently only encrypted implemented).
    public static final int DEFAULT_UNECRYPTED_UDP_PORT = 6666;
    public static final int DEFAULT_ECRYPTED_UDP_PORT = 6667;
    public static final int UDP_SOCKET_TIMEOUT = 60000;
    public static final int UDP_SOCKET_BUFFER_SIZE = 1024;

    // The default key to decrypt UDP broadcast messages.
    public static final String DEFAULT_UDP_KEY = "yGAdlopoPVldABfn";

}
