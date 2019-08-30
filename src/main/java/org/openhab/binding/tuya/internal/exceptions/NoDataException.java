/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.exceptions;

import java.io.IOException;

/**
 * Exception when unable to parse a packet or message.
 *
 * @author Wim Vissers.
 *
 */
public class NoDataException extends IOException {

    private static final long serialVersionUID = 7212214050617429052L;

    public NoDataException() {
    }

    public NoDataException(String message) {
        super(message);
    }

}
