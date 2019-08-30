/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.data;

import static org.openhab.binding.tuya.TuyaBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.annotations.Channel;
import org.openhab.binding.tuya.internal.discovery.DeviceDescriptor;

import com.google.gson.annotations.SerializedName;

/**
 * This is the description of the status of the Color LED device.
 *
 * @author Wim Vissers.
 *
 */
public class ColorLedState extends DeviceState {

    private Dps dps;

    public ColorLedState() {
    }

    public ColorLedState(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public ColorLedState withPower(Command command) {
        dps.dp1 = toBoolean(command);
        return this;
    }

    @Channel(CHANNEL_POWER)
    public OnOffType getPower() {
        return toOnOffType(dps.dp1);
    }

    public ColorLedState withColorMode(Command command) {
        dps.dp2 = toBoolean(command) == null ? null : toBoolean(command) ? "colour" : "white";
        return this;
    }

    @Channel(CHANNEL_COLOR_MODE)
    public OnOffType getColorMode() {
        return dps.dp2 == null ? null : dps.dp2.equals("colour") ? OnOffType.ON : OnOffType.OFF;
    }

    public ColorLedState withBrightness(Command command) {
        dps.dp3 = toInt8(command);
        dps.dp1 = dps.dp3 > 0;
        return this;
    }

    @Channel(CHANNEL_BRIGHTNESS)
    public DecimalType getBrightness() {
        return dps.dp3 == null ? null : toDecimalType(dps.dp3);
    }

    public ColorLedState withColorTemperature(Command command) {
        dps.dp4 = toInt8(command);
        return this;
    }

    @Channel(CHANNEL_COLOR_TEMPERATURE)
    public DecimalType getColorTemperature() {
        return dps.dp4 == null ? null : toDecimalType(dps.dp4);
    }

    public ColorLedState withColor(Command command) {
        dps.dp5 = toColorString(command);
        return this;
    }

    /**
     * The device properties. Please note that we use boxed classes here,
     * to allow them to be null. In case of setting properties, null properties
     * will not be serialized by Gson.
     *
     */
    public class Dps {

        /**
         * Lamp on/off.
         */
        @SerializedName("1")
        private Boolean dp1;

        /**
         * Mode: "white" or "color".
         */
        @SerializedName("2")
        private String dp2;

        /**
         * Brightness 0..255.
         */
        @SerializedName("3")
        private Integer dp3;

        /**
         * Color temperature 0..255.
         */
        @SerializedName("4")
        private Integer dp4;

        /**
         * Color as hex string.
         */
        @SerializedName("5")
        private String dp5;

        @SerializedName("9")
        private Integer dp9;

    }

}
