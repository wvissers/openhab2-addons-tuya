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
 * Raw packet, containing a buffer and actual data length.
 *
 * @author Wim Vissers.
 *
 */
public class Packet {

    private final byte[] buffer;
    private final int length;

    public Packet(byte[] buffer, int length) {
        this.buffer = buffer;
        this.length = length;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getLength() {
        return length;
    }

}
