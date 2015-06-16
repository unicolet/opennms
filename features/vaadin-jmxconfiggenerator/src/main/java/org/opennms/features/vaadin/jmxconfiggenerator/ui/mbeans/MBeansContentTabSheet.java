/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.vaadin.ui.TabSheet;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ModelChangeListener;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UIHelper;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 *
 * @author Markus von Rüden
 */
public class MBeansContentTabSheet extends TabSheet implements ModelChangeListener<Mbean>, ViewStateChangedListener {

	private AttributesLayout attributesLayout;
	private CompositesLayout compositesLayout;
	private final MBeansController controller;
	private int selectedTabPosition;

	public MBeansContentTabSheet(final MBeansController controller) {
		this.controller = controller;
		setSizeFull();
		attributesLayout = new AttributesLayout(controller);
		compositesLayout = new CompositesLayout(controller);
		attributesLayout.setSizeFull();
		compositesLayout.setSizeFull();
		
		addTab(attributesLayout, "Attributes");
		addTab(compositesLayout, "Composites");
	}

	@Override
	public void modelChanged(Mbean newModel) {
		//forward
		attributesLayout.modelChanged(newModel);
		compositesLayout.modelChanged(newModel);
		disableCompositesTabIfNecessary(newModel);
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		//just forward
		attributesLayout.viewStateChanged(event);
		compositesLayout.viewStateChanged(event);
		selectedTabPosition = UIHelper.enableTabs(this, event, selectedTabPosition);
		disableCompositesTabIfNecessary(controller.getSelectedMBean());
	}

	private void disableCompositesTabIfNecessary(Mbean newModel) {
		boolean alreadyDisabled = !getTab(compositesLayout).isEnabled();
		boolean shouldDisable = newModel == null || newModel.getCompAttrib().isEmpty();
		boolean disabled = shouldDisable || alreadyDisabled; //so we do not overwrite enable/disable while already disabled due to another component
		//disable composites tab, if there are no composites
		getTab(compositesLayout).setEnabled(!disabled);
		getTab(compositesLayout).setDescription(disabled ? "no composites available" : null);
	}

}
