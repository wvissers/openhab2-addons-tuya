/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.json;

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

    public Dps getDps() {
        return dps;
    }

    public void setDps(Dps dps) {
        this.dps = dps;
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

        public boolean isDp1() {
            return dp1;
        }

        public void setDp1(boolean dp1) {
            this.dp1 = dp1;
        }

        public int getDp2() {
            return dp2;
        }

        public void setDp2(int dp2) {
            this.dp2 = dp2;
            this.dp1 = dp2 > 0;
        }

        public int getDp3() {
            return dp3;
        }

        public void setDp3(int dp3) {
            this.dp3 = dp3;
        }
    }

}
