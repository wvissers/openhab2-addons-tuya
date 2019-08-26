/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.json;

import java.util.Date;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.DeviceDescriptor;

import com.google.gson.annotations.SerializedName;

/**
 * Basic template for status messages to/from devices.
 *
 * @author Wim Vissers.
 *
 */
public class JsonData {

    private String devId;

    @SerializedName("t")
    long time;

    public JsonData() {
    }

    public JsonData(DeviceDescriptor deviceDescriptor) {
        this.devId = deviceDescriptor.getGwId();
        this.time = new Date().getTime() / 1000;
    }

    // Conversion methods to be used in subclasses.

    protected OnOffType toOnOffType(Boolean bool) {
        return bool == null ? null : bool ? OnOffType.ON : OnOffType.OFF;
    }

    protected Boolean toBoolean(Command command) {
        if (command instanceof OnOffType) {
            return command == OnOffType.ON;
        } else {
            return null;
        }
    }

    /**
     * Take an OH command, and try to calculate the value as and integer
     * from 0 to 255. This is used to convert dimmer commands to the 0..255 value used by the Tuya devices.
     *
     * @param command the OH command.
     * @return the numeric value in the range 0..255.
     */
    protected Integer toInt8(Command command) {
        if (command instanceof Number) {
            return (int) ((Math.round(((Number) (command)).doubleValue() * 255)) & 0xFF);
        } else {
            return null;
        }
    }

    /**
     * Take an OH command represented as Color (HSBType) and convert it to a Tuya understandable RGB value.
     *
     * @param hsb the color to encode.
     * @return the command string.
     */
    protected String toColorString(Command command) {
        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            StringBuilder b = new StringBuilder();
            b.append(Integer.toHexString(hsb.getRed().intValue() * 255 / 100))
                    .append(Integer.toHexString(hsb.getGreen().intValue() * 255 / 100))
                    .append(Integer.toHexString(hsb.getBlue().intValue() * 255 / 100)).append("00f1ffff");// append("016500ff");
            return b.toString();
        } else {
            return null;
        }
    }

    public String getDevId() {
        return devId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
