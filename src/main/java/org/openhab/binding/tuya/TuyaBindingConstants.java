/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link JavaSystemBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Wim Vissers - Initial contribution
 */
public class TuyaBindingConstants {

    // Device properties that can be inspected with e.g. the Paper UI.
    public static final String PROPERTY_IP_ADDRESS = "ip-address";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_PRODUCT_KEY = "product-key";

    // Binding id.
    public static final String BINDING_ID = "tuya";

    // List of all Thing Type UIDs.
    public final static ThingTypeUID THING_TYPE_POWER_PLUG = new ThingTypeUID(BINDING_ID, "powerplug");
    public final static ThingTypeUID THING_TYPE_COLOR_LED = new ThingTypeUID(BINDING_ID, "colorled");
    public final static ThingTypeUID THING_TYPE_FILAMENT_LED = new ThingTypeUID(BINDING_ID, "filamentled");
    public final static ThingTypeUID THING_TYPE_SIREN = new ThingTypeUID(BINDING_ID, "siren");

    // List of all Channel ids.
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public final static String CHANNEL_COLOR_MODE = "colorMode";
    public final static String CHANNEL_ALARM = "alarm";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_DURATION = "duration";

    // Default API version (currently only 3.3 supported).
    public static final String DEFAULT_VERSION = "3.3";

}
