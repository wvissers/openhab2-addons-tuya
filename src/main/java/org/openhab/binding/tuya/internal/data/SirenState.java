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
        Volume result = Volume.getByName(dps.dp5);
        return result.getUntil() < 0.51 ? new DecimalType(result.getFrom()) : new DecimalType(result.getUntil());
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
         * Volume: mute, low, middle, high.
         */
        @SerializedName("5")
        private String dp5;

    }

    private enum Volume {
        MUTE(0.0, 0.25),
        LOW(0.26, 0.5),
        MIDDLE(0.51, 0.75),
        HIGH(0.76, 1.0),
        UNDEFINED(0.0, 0.0);

        private final double from;
        private final double until;

        private Volume(double from, double until) {
            this.from = from;
            this.until = until;
        }

        public double getFrom() {
            return from;
        }

        public double getUntil() {
            return until;
        }

        public String getName() {
            return name().toLowerCase();
        }

        public static final Volume getByLevel(double level) {
            for (Volume volume : Volume.values()) {
                if (level < volume.until) {
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
