/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.discovery;

import org.openhab.binding.tuya.handler.AbstractTuyaHandler;

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
     * The local encryption key must be set in the configuration. It is not transmitted by UDP.
     */
    private String localKey;
    private AbstractTuyaHandler handler;

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

    public String getLocalKey() {
        return localKey;
    }

    public DeviceDescriptor withLocalKey(String localKey) {
        this.localKey = localKey;
        return this;
    }

    public AbstractTuyaHandler getHandler() {
        return handler;
    }

    public void setHandler(AbstractTuyaHandler handler) {
        this.handler = handler;
    }

}
