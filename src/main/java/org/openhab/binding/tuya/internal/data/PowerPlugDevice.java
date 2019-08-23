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
public class PowerPlugDevice extends BasicDevice {

    private Dps dps;

    public PowerPlugDevice() {
    }

    public PowerPlugDevice(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public Dps getDps() {
        return dps;
    }

    public void setDps(Dps dps) {
        this.dps = dps;
    }

    public class Dps {

        @SerializedName("1")
        private boolean dp1;

        @SerializedName("9")
        private int dp9;

        public boolean isDp1() {
            return dp1;
        }

        public void setDp1(boolean dp1) {
            this.dp1 = dp1;
        }

        public int getDp9() {
            return dp9;
        }

        public void setDp9(int dp9) {
            this.dp9 = dp9;
        }

    }

}
