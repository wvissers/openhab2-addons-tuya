/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal;

import org.openhab.binding.tuya.internal.data.DeviceDatagram;

/**
 * Descriptor of the device in the repository.
 *
 * @author Wim Vissers.
 *
 */
public class DeviceDescriptor {

    /**
     * The datagram retrieved from the UDP broadcast.
     */
    private DeviceDatagram deviceDatagram;

    public DeviceDescriptor() {
    }

    public DeviceDescriptor(DeviceDatagram deviceDatagram) {
        this.deviceDatagram = deviceDatagram;
    }

    // Convenience methods.
    public String getIp() {
        return deviceDatagram.getIp();
    }

    public String getGwId() {
        return deviceDatagram.getGwId();
    }

    public String getVersion() {
        return deviceDatagram.getVersion();
    }

    public String getProductKey() {
        return deviceDatagram.getProductKey();
    }

    public boolean isEncrypt() {
        return deviceDatagram.isEncrypt();
    }

    // DeviceDatagram getters and setters
    public DeviceDatagram getDeviceDatagram() {
        return deviceDatagram;
    }

    public void setDeviceDatagram(DeviceDatagram deviceDatagram) {
        this.deviceDatagram = deviceDatagram;
    }

}
