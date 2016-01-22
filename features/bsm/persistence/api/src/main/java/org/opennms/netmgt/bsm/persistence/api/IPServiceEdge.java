/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.persistence.api;

import java.util.Objects;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.opennms.netmgt.model.OnmsMonitoredService;

@Entity
@Table(name = "bsm_service_ifservices")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue("ipservices")
public class IPServiceEdge extends BusinessServiceEdge  {

    private OnmsMonitoredService m_ipService;

    // NOTE: When we use @Column on this field, Hibernate attempts to serialize the objects as a byte array
    // Instead, we resort to use @ManyToOne
    @ManyToOne(optional=false)
    @JoinColumn(name="ifserviceid", nullable=false)
    public OnmsMonitoredService getIpService() {
        return m_ipService;
    }

    public void setIpService(OnmsMonitoredService ipService) {
        m_ipService = ipService;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        return OnmsMonitoredServiceHelper.getReductionKeys(m_ipService);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IPServiceEdge other = (IPServiceEdge) obj;

        // OnmsMonitoredService objects don't properly support the equals() and hashCode() methods
        // so we resort to comparing their IDs, which is sufficient in the case
        if (!super.equals(obj)) {
            return false;
        } else if (m_ipService == null && other.m_ipService != null) {
            return false;
        } else if (m_ipService != null && other.m_ipService == null) {
            return false;
        }

        return Objects.equals(m_ipService.getId(), other.m_ipService.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_ipService == null ? null : m_ipService.getId());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("ipService", m_ipService)
                .toString();
    }
}
