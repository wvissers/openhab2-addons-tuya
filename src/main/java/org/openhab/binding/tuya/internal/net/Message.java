/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import org.openhab.binding.tuya.internal.json.CommandByte;
import org.openhab.binding.tuya.internal.json.JsonColorLed;
import org.openhab.binding.tuya.internal.json.JsonDiscovery;
import org.openhab.binding.tuya.internal.json.JsonFilamentLed;
import org.openhab.binding.tuya.internal.json.JsonPowerPlug;

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
    private CommandByte commandByte;

    public Message(String data) {
        this.data = data;
    }

    public Message(byte[] payload, long sequenceNumber, long commandByte, String data) {
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
        this.commandByte = CommandByte.valueOf((int) commandByte);
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
        return gson.fromJson(getData(), JsonDiscovery.class);
    }

    /**
     * Try to parse the message data as a PowerPlug.
     *
     * @param message the message (returned previously by the parser).
     * @return the PowerPlug if possible.
     */
    public JsonPowerPlug toPowerPlug() {
        return gson.fromJson(getData(), JsonPowerPlug.class);
    }

    /**
     * Try to parse the message data as a ColorLed.
     *
     * @param message the message (returned previously by the parser).
     * @return the ColorLed if possible.
     */
    public JsonColorLed toColorLed() {
        return gson.fromJson(getData(), JsonColorLed.class);
    }

    /**
     * Try to parse the message data as a FilamentLed.
     *
     * @param message the message (returned previously by the parser).
     * @return the FilamentLed if possible.
     */
    public JsonFilamentLed toFilamentLed() {
        return gson.fromJson(getData(), JsonFilamentLed.class);
    }

}
