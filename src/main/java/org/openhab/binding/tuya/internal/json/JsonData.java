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

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
