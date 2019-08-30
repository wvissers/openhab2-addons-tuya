/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.data;

import org.openhab.binding.tuya.internal.discovery.JsonDiscovery;

import com.google.gson.Gson;

/**
 * Message received from the network.
 *
 * @author Wim Vissers.
 *
 */
public class Message {

    private byte[] payload;
    private final String data;
    private static final Gson GSON = new Gson();

    private long sequenceNumber;
    private CommandByte commandByte;

    public Message(String data) {
        this.data = data;
    }

    public Message(byte[] payload, long sequenceNumber, long commandByte, String data) {
        this(data);
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
        this.commandByte = CommandByte.valueOf((int) commandByte);
    }

    /**
     * The raw data may be just an error message, or decoded more complex data.
     * 
     * @return
     */
    public String getData() {
        return data;
    }

    /**
     * Return true is the message contains data that is probably json encoded. If this method returns false, it is
     * useless to try to parse it as json data. An empty json object is still valid data, so "{}" will also return true.
     *
     * @return true if the message contains data.
     */
    public boolean hasData() {
        return data != null && !data.isEmpty() && data.startsWith("{");
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

    public CommandByte getCommandByte() {
        return commandByte;
    }

    public void setCommandByte(CommandByte commandByte) {
        this.commandByte = commandByte;
    }

    /**
     * Try to parse the message data as a DeviceDatagram.
     *
     * @param message the message (returned previously by the parser).
     * @return the DeviceDatagram if possible.
     */
    public JsonDiscovery toJsonDiscovery() {
        return GSON.fromJson(getData(), JsonDiscovery.class);
    }

    /**
     * Try to parse the message data to the given class.
     *
     * @param       %lt;T&gt; This method converts the data to DeviceState or subclasses thereof, given by the target
     *                  class.
     *
     * @param clazz the target class.
     * @return
     * @return a new instance of clazz filled with the message data.
     */
    public <T extends DeviceState> T toDeviceState(Class<T> clazz) {
        return GSON.fromJson(getData(), clazz);
    }

}
