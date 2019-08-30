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
 * Exception when unable to parse a packet or message.
 *
 * @author Wim Vissers.
 *
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 2360268045997933969L;

    public ParseException(String message) {
        super(message);
    }

}
