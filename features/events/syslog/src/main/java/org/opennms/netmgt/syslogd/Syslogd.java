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

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * The received messages are converted into XML and sent to eventd.
 * </p>
 * <p>
 * <strong>Note: </strong>Syslogd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Syslogd has no impact on the receiving and
 * processing of syslog messages.
 * </p>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class Syslogd extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Syslogd.class);

    /**
     * The name of the logging category for Syslogd.
     */
    public static final String LOG4J_CATEGORY = "syslogd";

    private SyslogHandler m_udpEventReceiver;

    private BroadcastEventProcessor m_broadcastEventProcessor;

    @Autowired
    private SyslogdConfigFactory m_config;

    /**
     * <p>Constructor for Syslogd.</p>
     */
    public Syslogd() {
        super(LOG4J_CATEGORY);
    }

    public SyslogdConfigFactory getConfigFactory() {
        return m_config;
    }

    public void setConfigFactory(SyslogdConfigFactory config) {
        m_config = config;
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {

        try {
            // clear out the known nodes
            SyslogdIPMgrJDBCImpl.getInstance().dataSourceSync();
        } catch (SQLException e) {
            LOG.error("Failed to load known IP address list", e);
            throw new UndeclaredThrowableException(e);
        }

        m_udpEventReceiver = new SyslogHandler(m_config);

    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        LOG.debug("Starting SyslogHandler");
        m_udpEventReceiver.start();

        try {
            m_broadcastEventProcessor = new BroadcastEventProcessor();
        } catch (Throwable ex) {
            LOG.error("Failed to setup event reader", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        if (m_broadcastEventProcessor != null) {
            LOG.debug("stop: Stopping the Syslogd event receiver");
            m_broadcastEventProcessor.close();
            LOG.debug("stop: Stopped the Syslogd event receiver");
        }

        if (m_udpEventReceiver != null) {
            LOG.debug("stop: Stopping the Syslogd UDP receiver");
            m_udpEventReceiver.stop();
            LOG.debug("stop: Stopped the Syslogd UDP receiver");
        }
    }
}
