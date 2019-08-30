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
 * This is the description of the status of the Filament LED device.
 *
 * @author Wim Vissers.
 *
 */
public class FilamentLedState extends DeviceState {

    private Dps dps;

    public FilamentLedState() {
    }

    public FilamentLedState(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public FilamentLedState withPower(Command command) {
        dps.dp1 = toBoolean(command);
        return this;
    }

    @Channel(CHANNEL_POWER)
    public OnOffType getPower() {
        return toOnOffType(dps.dp1);
    }

    public FilamentLedState withBrightness(Command command) {
        dps.dp2 = toInt8(command);
        dps.dp1 = dps.dp2 > 0;
        return this;
    }

    @Channel(CHANNEL_BRIGHTNESS)
    public DecimalType getBrightness() {
        return dps.dp2 == null ? null : toDecimalType(dps.dp2);
    }

    public FilamentLedState withColorTemperature(Command command) {
        dps.dp3 = toInt8(command);
        return this;
    }

    @Channel(CHANNEL_COLOR_TEMPERATURE)
    public DecimalType getColorTemperature() {
        return dps.dp3 == null ? null : toDecimalType(dps.dp3);
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
         * Brightness 0..255.
         */
        @SerializedName("2")
        private Integer dp2;

        /**
         * Color temperature 0..255.
         */
        @SerializedName("3")
        private Integer dp3;

    }

}
