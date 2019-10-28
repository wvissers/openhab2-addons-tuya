/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.discovery;

/**
 * Datagram definition broadcasted by the device. It will be populated
 * using Gson, so setters are not required. Gson uses reflection to
 * set the value for private members.
 *
 * @author Wim Vissers.
 *
 */
public class JsonDiscovery {

    private String gwId;
    private Integer active;
    private Integer ability;
    private Boolean encrypt;
    private String productKey;
    private String version;
    private String ip;

    public JsonDiscovery() {
    }

    public JsonDiscovery(String gwId, String version, String ip) {
        this.gwId = gwId;
        this.version = version;
        this.ip = ip;
        encrypt = true;
    }

    public String getGwId() {
        return gwId;
    }

    public Integer getActive() {
        return active;
    }

    public Integer getAbility() {
        return ability;
    }

    public Boolean isEncrypt() {
        return encrypt;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getVersion() {
        return version;
    }

    public String getIp() {
        return ip;
    }
}
