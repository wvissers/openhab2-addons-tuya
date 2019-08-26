/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.json;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.DeviceDescriptor;

import com.google.gson.annotations.SerializedName;

/**
 * This is the description of the status of the Filament LED device.
 *
 * @author Wim Vissers.
 *
 */
public class JsonFilamentLed extends JsonData {

    private Dps dps;

    public JsonFilamentLed() {
    }

    public JsonFilamentLed(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public JsonFilamentLed withPower(Command command) {
        dps.dp1 = toBoolean(command);
        return this;
    }

    public OnOffType getPower() {
        return toOnOffType(dps.dp1);
    }

    public JsonFilamentLed withBrightness(Command command) {
        dps.dp2 = toInt8(command);
        dps.dp1 = dps.dp2 > 0;
        return this;
    }

    public JsonFilamentLed withColorTemperature(Command command) {
        dps.dp3 = toInt8(command);
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
