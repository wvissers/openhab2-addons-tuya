/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.util;

import java.util.Arrays;

/**
 * Helper class for debugging.
 *
 * @author Wim Vissers.
 *
 */
public class Debug {

    public static void print(byte[] buf, int length) {
        for (int i = 0; i < length && i < buf.length; i++) {
            System.out.print(String.format("%x", buf[i]));
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void print(String input) {
        print(input.getBytes(), input.length());
    }

    private static int valueOf(char ch) {
        if (Character.isDigit(ch)) {
            return Character.getNumericValue(ch);
        } else {
            switch (ch) {
                case 'a':
                case 'A':
                    return 10;
                case 'b':
                case 'B':
                    return 11;
                case 'c':
                case 'C':
                    return 12;
                case 'd':
                case 'D':
                    return 13;
                case 'e':
                case 'E':
                    return 14;
                case 'f':
                case 'F':
                    return 15;
                default:
                    return -1;
            }

        }
    }

    public static byte[] getBuffer(String input) {
        byte[] result = new byte[input.length() / 3];
        int j = 0;
        int val = 0;
        for (int i = 0; i < input.length(); i++) {
            int x = valueOf(input.charAt(i));
            if (x < 0) {
                if (j >= result.length) {
                    result = Arrays.copyOf(result, j + 1);
                }
                result[j] = (byte) (val & 0xFF);
                val = 0;
                j++;
            } else {
                val *= 16;
                val += x;
            }
        }
        result = Arrays.copyOf(result, j + 1);
        result[j] = (byte) (val & 0xFF);
        return result;
    }

}
