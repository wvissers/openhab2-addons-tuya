/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.data;

import org.openhab.binding.tuya.internal.DeviceDescriptor;

import com.google.gson.annotations.SerializedName;

// {"devId":"70116356840d8e5f1cb3","dps":{"1":false},"t":1566481749}
/**
 * This is the description of the status of the PowerPlug device.
 *
 * @author Wim Vissers.
 *
 */
public class ColorLedDevice {

    private String devId;
    private Dps dps;

    public ColorLedDevice() {
    }

    public ColorLedDevice(DeviceDescriptor deviceDescriptor) {
        this.devId = deviceDescriptor.getGwId();
        dps = new Dps();
    }

    @SerializedName("t")
    long time;

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public Dps getDps() {
        return dps;
    }

    public void setDps(Dps dps) {
        this.dps = dps;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * The device properties. Please note that we use boxed classes here,
     * to allow them to be null. In case of setting properties, null properties
     * will not be serialized by Gson.
     *
     */
    public class Dps {

        @SerializedName("1")
        private Boolean dp1;

        @SerializedName("3")
        private Integer dp3;

        @SerializedName("4")
        private Integer dp4;

        @SerializedName("9")
        private Integer dp9;

        public boolean isDp1() {
            return dp1;
        }

        public void setDp1(boolean dp1) {
            this.dp1 = dp1;
        }

        public int getDp3() {
            return dp3;
        }

        /**
         * In addition to setting the brightness 0..100, make sure the switch is
         * set accordingly.
         * 
         * @param dp3
         */
        public void setDp3(int dp3) {
            this.dp3 = dp3;
            this.dp1 = dp3 > 0;
        }

        public int getDp4() {
            return dp4;
        }

        public void setDp4(int dp4) {
            this.dp4 = dp4;
        }

        public int getDp9() {
            return dp9;
        }

        public void setDp9(int dp9) {
            this.dp9 = dp9;
        }

    }

}
