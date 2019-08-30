/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.exceptions;

/**
 * Unchecked exception for reporting unrecoverable errors.
 * 
 * @author wim
 *
 */
public class HandlerInitializationException extends RuntimeException {

    private static final long serialVersionUID = 2970404104689196085L;

    public HandlerInitializationException(String message) {
        super(message);
    }

}
