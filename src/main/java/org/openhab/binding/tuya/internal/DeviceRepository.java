/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.TuyaBindingConstants.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;

import org.openhab.binding.tuya.internal.json.JsonDiscovery;
import org.openhab.binding.tuya.internal.net.DatagramEventEmitter;
import org.openhab.binding.tuya.internal.net.Message;
import org.openhab.binding.tuya.internal.net.Packet;
import org.openhab.binding.tuya.internal.util.MessageParser;
import org.openhab.binding.tuya.internal.util.ParseException;
import org.openhab.binding.tuya.internal.util.SingletonEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The device repository to hold details of the devices discovered and/or registered.
 *
 * @author Wim Vissers.
 *
 */
public class DeviceRepository extends SingletonEventEmitter<String, DeviceDescriptor, Boolean> {

    private MessageParser parser;
    /**
     * The singleton instance.
     */
    private static final DeviceRepository INSTANCE = new DeviceRepository();
    /**
     * Listener for UDP packets transmitted to advertise devices.
     */
    private DatagramEventEmitter encryptedListener;
    /**
     * The logger instance.
     */
    private Logger logger = LoggerFactory.getLogger(DeviceRepository.class);
    /**
     * Discovered/registered devices. Key is the device id.
     */
    private final ConcurrentHashMap<String, DeviceDescriptor> devices;

    /**
     * Private constructor. It's a singleton.
     */
    private DeviceRepository() {
        devices = new ConcurrentHashMap<>();
        parser = new MessageParser(DEFAULT_VERSION, DEFAULT_UDP_KEY);
    }

    /**
     * Get the single instance of this repository.
     *
     * @return the singleton instance.
     */
    public static DeviceRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Start the threads, using the given executor service.
     *
     * @param scheduler the executer service to use.
     */
    public void start(ScheduledExecutorService scheduler) {
        if (encryptedListener == null) {
            encryptedListener = new DatagramEventEmitter(DEFAULT_ECRYPTED_UDP_PORT);
            encryptedListener.on(DatagramEventEmitter.Event.UDP_PACKET_RECEIVED, (event, packet) -> {
                return processPacket(packet);
            });
            encryptedListener.start(scheduler);
        }
    }

    /**
     * Stop the running threads, if any.
     */
    @Override
    public void stop() {
        if (encryptedListener != null) {
            encryptedListener.stop();
            encryptedListener = null;
        }
    }

    /**
     * Process incoming UDP packet.
     *
     * @param packet the packet.
     */
    private boolean processPacket(Packet packet) {
        try {
            List<Message> udpMessages = parser.parse(packet.getBuffer(), packet.getLength());
            for (Message message : udpMessages) {
                JsonDiscovery jd = message.toJsonDiscovery();
                DeviceDescriptor dd = devices.get(jd.getGwId());
                if (dd == null) {
                    dd = new DeviceDescriptor(jd);
                    devices.put(jd.getGwId(), dd);
                    emit(jd.getGwId(), dd);
                    logger.info("Add device '{}' with IP address '{}' to the repository", jd.getGwId(), jd.getIp());
                }
            }
            return true;
        } catch (ParseException e) {
            logger.error("UDP packet could not be parsed", e);
            return false;
        }
    }

    /**
     * Return the device descriptor of the given gwId. Usually the gwId is the same as the devId for standalone devices.
     *
     * @param gwId the gwId or devId.
     * @return the device descriptor, or null if not found.
     */
    public DeviceDescriptor getDeviceDescriptor(String gwId) {
        return devices.get(gwId);
    }

    /**
     * When a new handler is added, emit all the already discovered devices.
     */
    @Override
    protected void handlerAdded(String gwId, BiFunction<String, DeviceDescriptor, Boolean> eventCallback) {
        devices.forEach((key, descriptor) -> {
            eventCallback.apply(gwId, descriptor);
        });
    }

}
