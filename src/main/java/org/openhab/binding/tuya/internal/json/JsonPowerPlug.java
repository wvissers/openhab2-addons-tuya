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

// {"devId":"70116356840d8e5f1cb3","dps":{"1":false},"t":1566481749}
/**
 * This is the description of the status of the PowerPlug device.
 *
 * @author Wim Vissers.
 *
 */
public class JsonPowerPlug extends JsonData {

    private Dps dps;

    public JsonPowerPlug() {
    }

    public JsonPowerPlug(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public Dps getDps() {
        return dps;
    }

    public void setDps(Dps dps) {
        this.dps = dps;
    }

    public JsonPowerPlug withPower(Command command) {
        dps.dp1 = toBoolean(command);
        return this;
    }

    public OnOffType getPower() {
        return toOnOffType(dps.dp1);
    }

    public class Dps {

        @SerializedName("1")
        private Boolean dp1;

        @SerializedName("9")
        private Integer dp9;

    }

}
