/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal;

import org.openhab.binding.tuya.internal.json.JsonDiscovery;

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
    private JsonDiscovery jsonDiscovery;
    /**
     * If this device is using heart beat messages to keep the connection alive. Default is true.
     */
    private boolean keepAlive = true;

    public DeviceDescriptor() {
    }

    public DeviceDescriptor(JsonDiscovery jsonDiscovery) {
        this.jsonDiscovery = jsonDiscovery;
    }

    // Convenience methods.
    public String getIp() {
        return jsonDiscovery.getIp();
    }

    public String getGwId() {
        return jsonDiscovery.getGwId();
    }

    public String getVersion() {
        return jsonDiscovery.getVersion();
    }

    public String getProductKey() {
        return jsonDiscovery.getProductKey();
    }

    public boolean isEncrypt() {
        return jsonDiscovery.isEncrypt();
    }

    // JsonDiscovery getters and setters
    public JsonDiscovery getJsonDiscovery() {
        return jsonDiscovery;
    }

    public void setDeviceDatagram(JsonDiscovery deviceDatagram) {
        this.jsonDiscovery = deviceDatagram;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public DeviceDescriptor withKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

}
