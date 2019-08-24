/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;

import org.openhab.binding.tuya.internal.exceptions.ParseException;
import org.openhab.binding.tuya.internal.json.CommandByte;
import org.openhab.binding.tuya.internal.net.Message;
import org.openhab.binding.tuya.internal.net.Packet;

/**
 * Parser for messages, with decryption where needed. Hence, a parser
 * is device dependent.
 *
 * @author Wim Vissers.
 *
 */
public class MessageParser {

    private static final int HEADER_SIZE = 16;

    // Helper class instances.
    private TuyaCipher cipher;
    private final String version;

    public MessageParser(String version, String key) {
        try {
            cipher = new TuyaCipher(key);
        } catch (UnsupportedEncodingException e) {
            // Should not happen.
        }
        this.version = version;
    }

    public MessageParser(String version, byte[] key) {
        cipher = new TuyaCipher(key);
        this.version = version;
    }

    /**
     * The buffer may contain multiple packets. The
     * packets are returned as a List of Message.
     *
     * @param buffer the byte array received.
     * @return the List with packets.
     * @throws ParseException
     */
    public List<Message> parse(byte[] buffer, int length) throws ParseException {
        List<Message> result = new ArrayList<>();
        parseRecursive(result, buffer, length);
        return result;
    }

    /**
     * Parse the packet.
     *
     * @param packet the packet to parse.
     * @return a List of Message.
     * @throws ParseException when anything goes wrong.
     */
    public List<Message> parse(Packet packet) throws ParseException {
        return parse(packet.getBuffer(), packet.getLength());
    }

    private void parseRecursive(List<Message> result, byte[] buffer, int length) throws ParseException {
        int leftover = parsePacket(result, buffer, 0, length);
        while (leftover < length) {
            leftover = parsePacket(result, buffer, leftover, length);
        }
    }

    private int parsePacket(List<Message> result, byte[] buffer, int start, int length) throws ParseException {
        // Check for length
        // At minimum requires: prefix (4), sequence (4), command (4), length (4),
        // CRC (4), and suffix (4) for 24 total bytes
        // Messages from the device also include return code (4), for 28 total bytes
        if (length < 24) {
            throw new ParseException("Packet too short. Length: " + length);
        }

        // Check for prefix
        long prefix = BufferUtils.getUInt32(buffer, 0);
        if (prefix != 0x000055AA) {
            throw new ParseException("Prefix does not match: " + String.format("%x", prefix));
        }

        // Check for extra data
        int leftover = 0;

        // Leftover points to beginning of next packet, if any.
        int suffixLocation = BufferUtils.indexOfUInt32(buffer, 0x0000AA55);
        leftover = suffixLocation + 4;

        // Get sequence number
        long sequenceNumber = BufferUtils.getUInt32(buffer, 4);

        // Get command byte
        long commandByte = BufferUtils.getUInt32(buffer, 8);

        // Get payload size
        long payloadSize = BufferUtils.getUInt32(buffer, 12);

        // Check for payload
        if (leftover - 8 < payloadSize) {
            throw new ParseException("Packet missing payload: payload has length: " + payloadSize);
        }

        // Get the return code, 0 = success
        // This field is only present in messages from the devices
        // Absent in messages sent to device
        long returnCode = BufferUtils.getUInt32(buffer, 16);

        // Get the payload
        // Adjust for messages lacking a return code
        byte[] payload;
        boolean correct = false;
        if ((returnCode & 0xFFFFFF00) != 0) {
            payload = Arrays.copyOfRange(buffer, HEADER_SIZE, (int) (HEADER_SIZE + payloadSize - 8));
        } else if (commandByte == CommandByte.STATUS.getValue()) {
            correct = true;
            payload = Arrays.copyOfRange(buffer, HEADER_SIZE + 3, (int) (HEADER_SIZE + payloadSize - 8));
        } else {
            payload = Arrays.copyOfRange(buffer, HEADER_SIZE + 4, (int) (HEADER_SIZE + payloadSize - 8));
        }

        // Check CRC
        long expectedCrc = BufferUtils.getUInt32(buffer, (int) (HEADER_SIZE + payloadSize - 8));
        long computedCrc = Crc.crc32(Arrays.copyOfRange(buffer, 0, (int) (payloadSize + 8)));

        if (computedCrc != expectedCrc) {
            throw new ParseException("Crc error. Expected: " + expectedCrc + ", computed: " + computedCrc);
        }
        try {
            // String data = new String(cipher.decrypt(payload), "UTF-8");
            byte[] data = cipher.decrypt(payload);
            String text = correct ? new String(data, 16, data.length - 16) : new String(data, "UTF-8");
            result.add(new Message(payload, sequenceNumber, commandByte, text));
        } catch (UnsupportedEncodingException | IllegalBlockSizeException e) {
            result.add(new Message(payload, sequenceNumber, commandByte, new String(payload)));
        }
        return length;
    }

    public byte[] encode(byte[] input, CommandByte command, long sequenceNo) {
        byte[] payload = null;
        // Version 3.3 is always encrypted.
        if (version.equals("3.3")) {
            payload = cipher.encrypt(input);
            // Check if we need an extended header. Depends on command.
            if (!command.equals(CommandByte.DP_QUERY)) {
                // Add 3.3 header.
                byte[] buffer = new byte[payload.length + 15];
                BufferUtils.fill(buffer, (byte) 0x00, 0, 15);
                BufferUtils.copy(buffer, "3.3", 0);
                BufferUtils.copy(buffer, payload, 15);
                payload = buffer;
            }
        } else {
            // todo: older protocols
            payload = input;
        }

        // Allocate buffer with room for payload + 24 bytes for
        // prefix, sequence, command, length, crc, and suffix
        byte[] buffer = new byte[payload.length + 24];

        // Add prefix, command and length.
        BufferUtils.putUInt32(buffer, 0, 0x000055AA);
        BufferUtils.putUInt32(buffer, 8, command.getValue());
        BufferUtils.putUInt32(buffer, 12, payload.length + 8);

        // Optionally add sequence number.
        if (sequenceNo >= 0) {
            BufferUtils.putUInt32(buffer, 4, sequenceNo);
        }

        // Add payload, crc and suffix
        BufferUtils.copy(buffer, payload, 16);
        byte[] crcbuf = new byte[payload.length + 16];
        BufferUtils.copy(crcbuf, buffer, 0, payload.length + 16);
        BufferUtils.putUInt32(buffer, payload.length + 16, Crc.crc32(crcbuf));
        System.out.println(Crc.crc32(crcbuf));
        BufferUtils.putUInt32(buffer, payload.length + 20, 0x0000AA55);

        return buffer;
    }

}
