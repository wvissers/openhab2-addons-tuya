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

    public static final String BINDING_ID = "tuya";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "powerplug");

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";

}
