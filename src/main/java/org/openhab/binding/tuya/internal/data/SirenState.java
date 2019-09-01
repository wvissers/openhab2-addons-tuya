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
public class SirenState extends DeviceState {

    private Dps dps;

    public SirenState() {
    }

    public SirenState(DeviceDescriptor deviceDescriptor) {
        super(deviceDescriptor);
        dps = new Dps();
    }

    public SirenState withAlarm(Command command) {
        dps.dp13 = toBoolean(command);
        return this;
    }

    @Channel(CHANNEL_ALARM)
    public OnOffType getPower() {
        return toOnOffType(dps.dp13);
    }

    public SirenState withVolume(Command command) {
        dps.dp5 = Volume.getByLevel(((DecimalType) (command)).doubleValue()).getName();
        return this;
    }

    @Channel(CHANNEL_VOLUME)
    public DecimalType getVolume() {
        return new DecimalType(Volume.getByName(dps.dp5).getValue());
    }

    public SirenState withDuration(Command command) {
        dps.dp7 = ((DecimalType) command).intValue();
        dps.dp7 = dps.dp7 < 1 ? 1 : dps.dp7 > 30 ? 30 : dps.dp7;
        return this;
    }

    @Channel(CHANNEL_DURATION)
    public DecimalType getDuration() {
        return new DecimalType(dps.dp7);
    }

    /**
     * The device properties. Please note that we use boxed classes here,
     * to allow them to be null. In case of setting properties, null properties
     * will not be serialized by Gson.
     *
     */
    public class Dps {

        /**
         * Alarm on/off.
         */
        @SerializedName("13")
        private Boolean dp13;

        /**
         * Alarm duration 1..30 seconds.
         */
        @SerializedName("7")
        private Integer dp7;

        /**
         * Volume: mute, low, middle, high.
         */
        @SerializedName("5")
        private String dp5;

    }

    private enum Volume {
        MUTE(0.0),
        LOW(0.33),
        MIDDLE(0.66),
        HIGH(1.0),
        UNDEFINED(0.0);

        private final double value;

        private Volume(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public String getName() {
            return name().toLowerCase();
        }

        public static final Volume getByLevel(double level) {
            for (Volume volume : Volume.values()) {
                if (level < volume.value + 0.165) {
                    return volume;
                }
            }
            return UNDEFINED;
        }

        public static final Volume getByName(String name) {
            if (name == null) {
                return UNDEFINED;
            }
            for (Volume volume : Volume.values()) {
                if (volume.getName().equals(name)) {
                    return volume;
                }
            }
            return UNDEFINED;
        }
    }

}
