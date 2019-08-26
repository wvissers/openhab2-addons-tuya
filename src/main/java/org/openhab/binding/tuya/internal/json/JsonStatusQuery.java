/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.json;

import org.openhab.binding.tuya.internal.DeviceDescriptor;

/**
 * template for a status request.
 *
 * @author Wim Vissers.
 *
 */
public class JsonStatusQuery extends JsonData {

    private String gwId;

    public JsonStatusQuery(DeviceDescriptor device) {
        super(device);
        this.gwId = device.getGwId();
    }

    public String getGwId() {
        return gwId;
    }

}
