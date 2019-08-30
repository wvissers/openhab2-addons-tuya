/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.util;

import java.nio.ByteBuffer;

import org.openhab.binding.tuya.internal.exceptions.ParseException;

/**
 * Utility class for buffer operations.
 *
 * @author Wim Vissers.
 *
 */
public class BufferUtils {

    /**
     * Extract the contents of the byte buffer as a new array for further processing
     * in byte array oriented APis. In particular, the bytes between position() and
     * limit() are copied.The position in the buffer is increased by the number of
     * remaining bytes.
     *
     * @param buffer the buffer.
     * @return the copy of the contents of the buffer as byte array.
     */
    public static byte[] getBytes(ByteBuffer buffer) {
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    /**
     * Get an unsigned 4 bytes number from the byte buffer.
     *
     * @param buffer the buffer containing the bytes.
     * @param start  the start index (0-based).
     * @return the number, reading 4 bytes from start to start + 4.
     * @throws ParseException
     */
    public static long getUInt32(byte[] buffer, int start) throws ParseException {
        if (buffer.length - start - 4 < 0) {
            throw new ParseException("Buffer too short.");
        }
        long result = 0;
        for (int i = start; i < start + 4; i++) {
            result *= 256;
            result += (buffer[i] & 0xff);
        }
        return result;
    }

    /**
     * Write an unsigned 4 bytes to the byte buffer.
     *
     * @param buffer the byte buffer.
     * @param start  the start index.
     * @param value  the number to store.
     */
    public static void putUInt32(byte[] buffer, int start, long value) {
        long lv = value;
        for (int i = 3; i >= 0; i--) {
            buffer[start + i] = (byte) (((lv & 0xFFFFFFFF) % 0x100) & 0xFF);
            lv /= 0x100;
        }
    }

    /**
     * Get the position of the marker.
     *
     * @param buffer
     * @param marker
     * @return
     */
    public static int indexOfUInt32(byte[] buffer, long marker) {
        long mrk = marker;
        byte[] m = new byte[4];
        for (int i = 3; i >= 0; i--) {
            m[i] = (byte) (mrk & 0xFF);
            mrk /= 256;
        }
        int j = 0;
        for (int p = 0; p < buffer.length; p++) {
            if (buffer[p] == m[j]) {
                if (j == 3) {
                    return p - 3;
                } else {
                    j++;
                }
            } else {
                j = 0;
            }
        }
        return -1;
    }

    /**
     * Copy from the source to the buffer, starting at index from.
     *
     * @param buffer the target buffer.
     * @param source the source.
     * @param from   the starting index in the target buffer.
     */
    public static byte[] copy(byte[] buffer, byte[] source, int from) {
        for (int i = 0; i < source.length; i++) {
            buffer[i + from] = source[i];
        }
        return buffer;
    }

    /**
     * Copy from the source to the buffer, starting at index from.
     *
     * @param buffer the target buffer.
     * @param source the source.
     * @param from   the starting index in the target buffer.
     */
    public static byte[] copy(byte[] buffer, byte[] source, int from, int length) {
        for (int i = 0; i < length; i++) {
            buffer[i + from] = source[i];
        }
        return buffer;
    }

    /**
     * Copy from the source to the buffer, starting at index from.
     *
     * @param buffer the target buffer.
     * @param source the source.
     * @param from   the starting index in the target buffer.
     */
    public static byte[] copy(byte[] buffer, String source, int from) {
        return copy(buffer, source.getBytes(), from);
    }

    /**
     * Fill with constant value, in the range from to until.
     *
     * @param buffer the target buffer.
     * @param fill   the fill byte.
     * @param from   the starting index in the target buffer.
     * @param length the length in the target buffer.
     */
    public static byte[] fill(byte[] buffer, byte fill, int from, int length) {
        for (int i = from; i < from + length; i++) {
            buffer[i] = fill;
        }
        return buffer;
    }

}
