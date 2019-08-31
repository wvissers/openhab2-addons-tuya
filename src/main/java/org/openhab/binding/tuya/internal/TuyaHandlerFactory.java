/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.TuyaBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.tuya.handler.ColorLedHandler;
import org.openhab.binding.tuya.handler.FilamentLedHandler;
import org.openhab.binding.tuya.handler.PowerPlugHandler;
import org.openhab.binding.tuya.handler.SirenHandler;

/**
 * The {@link TuyaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Wim Vissers - Initial contribution
 */
public class TuyaHandlerFactory extends BaseThingHandlerFactory {

    private static Set<ThingTypeUID> supportedThingTypes;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (supportedThingTypes == null) {
            supportedThingTypes = new HashSet<>();
            supportedThingTypes.add(THING_TYPE_POWER_PLUG);
            supportedThingTypes.add(THING_TYPE_COLOR_LED);
            supportedThingTypes.add(THING_TYPE_FILAMENT_LED);
            supportedThingTypes.add(THING_TYPE_SIREN);
        }
        return supportedThingTypes.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_POWER_PLUG)) {
            return new PowerPlugHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_COLOR_LED)) {
            return new ColorLedHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_FILAMENT_LED)) {
            return new FilamentLedHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SIREN)) {
            return new SirenHandler(thing);
        }

        return null;
    }
}
