/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate getters in the DeviceDescriptor objects to facilitate generic handling of properties.
 *
 * @author Wim Vissers
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Property {

    /**
     * The property name.
     *
     * @return the property name.
     */
    public String value() default "";

    /**
     * The display value when the property is null.
     * 
     * @return the display value.
     */
    public String nullValue() default "unknown";

}
