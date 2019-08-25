/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.discovery;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.tuya.internal.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The TuyaDiscoveryService is responsible for auto detecting Tuya broadcasts on
 * the local network. It builds a repository of devices for use by the various Tuya devices.
 *
 * @author Wim Vissers - Initial contribution
 */
public class TuyaDiscoveryService extends AbstractDiscoveryService {

    private static Set<ThingTypeUID> supportedThingsTypes;
    private Logger logger = LoggerFactory.getLogger(TuyaDiscoveryService.class);

    public static Set<ThingTypeUID> getSupportedTypes() {
        if (supportedThingsTypes == null) {
            supportedThingsTypes = new HashSet<>();
        }
        return supportedThingsTypes;
    }

    public TuyaDiscoveryService() {
        super(getSupportedTypes(), 5, true);
    }

    public void activate() {
        logger.debug("Starting Tuya discovery...");
        // removeOlderResults(System.currentTimeMillis(), getSupportedTypes());
        startBackgroundDiscovery();
    }

    @Override
    public void deactivate() {
        logger.debug("Stopping Tuya discovery...");
        stopBackgroundDiscovery();
        stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        DeviceRepository.getInstance().start(scheduler);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        DeviceRepository.getInstance().stop();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device search...");
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

}
