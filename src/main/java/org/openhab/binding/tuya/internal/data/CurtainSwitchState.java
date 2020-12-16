/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.data;

import static org.openhab.binding.tuya.TuyaBindingConstants.CHANNEL_POWER;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tuya.internal.annotations.Channel;
import org.openhab.binding.tuya.internal.discovery.DeviceDescriptor;
import org.openhab.binding.tuya.internal.net.QueueItem;

import com.google.gson.annotations.SerializedName;

// {"devId":"70116356840d8e5f1cb3","dps":{"1":false},"t":1566481749}
/**
 * This is the description of the status of the PowerPlug device.
 *
 * @author Gert Van Hoecke
 *
 */
public class CurtainSwitchState extends DeviceState {

    private Dps dps;

    public CurtainSwitchState() {
        dps = new Dps();
    }

    public CurtainSwitchState(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public Dps getDps() {
        return dps;
    }

    public void setDps(Dps dps) {
        this.dps = dps;
    }

    public CurtainSwitchState withPower(Command command) {
        dps.dp1 = toBoolean(command);
        return this;
    }

    @Channel(CHANNEL_POWER)
    public OnOffType getPower() {
        return toOnOffType(dps.dp1);
    }

    /**
     * Return true when the given QueueItem is conflicting with this item. This test is used to remove conflicting items
     * from the queue. An example is a switch that may be on or off, and it makes no sense to have both an on and an off
     * command in the queue at the same time.
     * 
     * @param other the item to compare to.
     * @return true when conflicting.
     */
    @Override
    public boolean isConflicting(QueueItem other) {
        DeviceState ds = other == null ? null : other.getDeviceState();
        return ds == null ? false : ds.getClass().equals(getClass()) && !((CurtainSwitchState) ds).dps.dp1.equals(dps.dp1);
    }

    public class Dps {

        @SerializedName("1")
        private Boolean dp1;

        @SerializedName("9")
        private Integer dp9;

    }

}
