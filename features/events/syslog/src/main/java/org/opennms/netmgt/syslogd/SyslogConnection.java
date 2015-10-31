/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SyslogConnection class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class SyslogConnection implements Callable<Callable<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogConnection.class);

    private final DatagramPacket _packet;

    private final SyslogdConfig _config;

    /**
     * <p>Constructor for SyslogConnection.</p>
     *
     * @param packet a {@link java.net.DatagramPacket} object.
     * @param matchPattern a {@link java.lang.String} object.
     * @param hostGroup a int.
     * @param messageGroup a int.
     * @param ueiList a {@link org.opennms.netmgt.config.syslogd.UeiList} object.
     * @param hideMessages a {@link org.opennms.netmgt.config.syslogd.HideMessage} object.
     * @param discardUei a {@link java.lang.String} object.
     */
    public SyslogConnection(final DatagramPacket packet, final SyslogdConfig config) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet cannot be null");
        } else if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        _packet = copyPacket(packet);
        _config = config;
    }

    public SyslogConnection(final InetSocketAddress source, final ByteBuffer buffer, final SyslogdConfig config) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        } else if (buffer == null) {
            throw new IllegalArgumentException("Buffer cannot be null");
        } else if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        _packet = copyPacket(source, buffer);
        _config = config;
    }

    /**
     * <p>call</p>
     */
    @Override
    public SyslogProcessor call() {

        ConvertToEvent re = null;
        try {
            re = new ConvertToEvent(
                _packet,
                _config
            );

            LOG.debug("Sending received packet to the SyslogProcessor queue");

            return new SyslogProcessor(re.getEvent(), _config.getNewSuspectOnMessage());

        } catch (final UnsupportedEncodingException e1) {
            LOG.debug("Failure to convert package", e1);
        } catch (final MessageDiscardedException e) {
            LOG.debug("Message discarded, returning without enqueueing event.", e);
        }
        return null;
    }

    private static DatagramPacket copyPacket(final DatagramPacket packet) {
        byte[] message = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(packet.getAddress().getHostName(), packet.getAddress().getAddress());
            DatagramPacket retPacket = new DatagramPacket(
                                                          message,
                                                          packet.getOffset(),
                                                          packet.getLength(),
                                                          addr,
                                                          packet.getPort()
                                                      );
                                                      return retPacket;
        } catch (UnknownHostException e) {
            LOG.warn("unable to clone InetAddress object for {}", packet.getAddress());
        }
        return null;
    }

    private static DatagramPacket copyPacket(final InetSocketAddress source, final ByteBuffer buffer) {
        byte[] message = new byte[0xffff];
        int i = 0;
        // Copy the buffer into the byte array
        while (buffer.hasRemaining()) {
            message[i++] = buffer.get();
        }
        DatagramPacket retPacket = new DatagramPacket(
            message,
            0,
            i,
            source.getAddress(),
            source.getPort()
        );
        return retPacket;
    }
}
