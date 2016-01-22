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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.opennms.netmgt.bsm.mapreduce.api.Edge;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Base edges that includes properties common to all edge types.
 *
 * // TODO MVR it must work. There is something else wrong. We must fix this (e.g. if this class is abstract sometimes edgeDao.findAll() works, sometimes it does not)
 * // The wiered thing is, that it only breaks after values are stored. So maybe it is a missing/wrong annotation.
 * Ideally this class would be abstract, but in some cases Hibernate may
 * try to instantiate this class.
 *
 * @author jwhite
 */
@Entity
@Table(name = "bsm_service_edge")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="type", discriminatorType= DiscriminatorType.STRING)
@DiscriminatorValue(value="")
public class BusinessServiceEdge implements Edge {

    public static final int DEFAULT_WEIGHT = 1;

    private Long m_id;

    // The Business Service reference where this edge belongs to!
    private BusinessServiceEntity m_businessService;

    private boolean m_enabled = true;

    private int m_weight = DEFAULT_WEIGHT;

    private AbstractMapFunction m_mapFunction;

    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    @Column(name = "id", nullable = false)
    public Long getId() {
        return m_id;
    }

    public void setId(Long id) {
        m_id = id;
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="bsm_service_id")
    public BusinessServiceEntity getBusinessService() {
        return m_businessService;
    }

    public void setBusinessService(BusinessServiceEntity service) {
        m_businessService = Objects.requireNonNull(service);
    }

    @Column(name = "enabled", nullable = false)
    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    @Column(name = "weight", nullable = false)
    public int getWeight() {
        return m_weight;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        // TODO MVR ???
        return Sets.newHashSet();
    }

    public void setWeight(int weight) {
        Preconditions.checkArgument(weight > 0, "weight must be strictly positive.");
        m_weight = weight;
    }

    // TODO MVR verify cascadetype
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "bsm_map_id")
    public AbstractMapFunction getMapFunction() {
        return m_mapFunction;
    }

    public void setMapFunction(AbstractMapFunction mapFunction) {
        m_mapFunction = Objects.requireNonNull(mapFunction);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessServiceEdge other = (BusinessServiceEdge) obj;

        return Objects.equals(m_id, other.m_id)
                && Objects.equals(m_businessService, other.m_businessService)
                && Objects.equals(m_enabled, other.m_enabled)
                && Objects.equals(m_weight, other.m_weight)
                && Objects.equals(m_mapFunction, other.m_mapFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id, m_enabled, m_weight, m_weight);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", m_id)
//                .add("businessService", m_businessService) // TODO MVR this results in a stack overflow exception
                .add("enabled", m_enabled)
                .add("weight", m_weight)
                .add("mapFunction", m_mapFunction)
                .toString();
    }
}
