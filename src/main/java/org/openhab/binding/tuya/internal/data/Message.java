/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.data;

import com.google.gson.Gson;

/**
 * Message received from the network.
 *
 * @author Wim Vissers.
 *
 */
public class Message {

    private byte[] payload;
    private String data;
    private static Gson gson;

    private long sequenceNumber;
    private long commandByte;

    public Message(byte[] payload, long sequenceNumber, long commandByte, String data) {
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
        this.commandByte = commandByte;
        this.data = data;
        if (gson == null) {
            gson = new Gson();
        }
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getCommandByte() {
        return commandByte;
    }

    public void setCommandByte(long commandByte) {
        this.commandByte = commandByte;
    }

    /**
     * Try to parse the message data as a DeviceDatagram.
     *
     * @param message the message (returned previously by the parser).
     * @return the DeviceDatagram if possible.
     */
    public DeviceDatagram toDeviceDatagram() {
        return gson.fromJson(getData(), DeviceDatagram.class);
    }

    /**
     * Try to parse the message data as a PowerPlugDevice.
     *
     * @param message the message (returned previously by the parser).
     * @return the PowerPlugDevice if possible.
     */
    public PowerPlugDevice toPowerPlugDevice() {
        return gson.fromJson(getData(), PowerPlugDevice.class);
    }

}
