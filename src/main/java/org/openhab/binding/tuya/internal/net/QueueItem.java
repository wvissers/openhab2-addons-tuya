/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.net;

import org.openhab.binding.tuya.internal.data.CommandByte;
import org.openhab.binding.tuya.internal.data.DeviceState;
import org.openhab.binding.tuya.internal.util.MessageParser;

/**
 * Item to populate the TuyaClient send queue.
 * 
 * @author Wim Vissers - Initial contribution.
 *
 */
public class QueueItem {

    private final CommandByte commandByte;
    private final DeviceState deviceState;

    public QueueItem(DeviceState deviceState, CommandByte commandByte) {
        this.deviceState = deviceState;
        this.commandByte = commandByte;
    }

    public CommandByte getCommandByte() {
        return commandByte;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    /**
     * Encode the item for sending.
     * 
     * @param messageParser the message parser (depends on the thing).
     * @param sequenceNo    sequence number provided by the Tuya client.
     * @return the byte array, ready to send.
     */
    public byte[] encode(MessageParser messageParser, long sequenceNo) {
        String data = deviceState == null ? "" : deviceState.toJson();
        return messageParser.encode(data.getBytes(), commandByte, sequenceNo);
    }

    /**
     * Return true when the given QueueItem is conflicting with this item. This test is used to remove conflicting items from the queue. An example is a switch that may be on or off, and it makes no sense to have both an on and an off command in the queue at the same time.
     * @param other the item to compare to.
     * @return true when conflicting.
     */
    public boolean isConflicting(QueueItem other) {
        return deviceState == null ? false : deviceState.isConflicting(other);
    }

}
