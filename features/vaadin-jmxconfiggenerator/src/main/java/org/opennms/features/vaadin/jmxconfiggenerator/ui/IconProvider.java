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

package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import org.opennms.features.vaadin.jmxconfiggenerator.Config;

/**
 * This class provides the application with icons. If any icon changes or new
 * icons are needed, please put them below.
 * 
 * @author Markus von Rüden
 */
public abstract class IconProvider {

	public static final String BUTTON_EDIT = "crystal_project/button_edit.png";
	public static final String BUTTON_SAVE = "crystal_project/button_save.png";
	public static final String BUTTON_CANCEL = "crystal_project/button_cancel.png";
	public static final Resource BUTTON_NEXT = FontAwesome.CHEVRON_RIGHT;
	public static final Resource BUTTON_PREVIOUS = FontAwesome.CHEVRON_LEFT;

	public static final String WORK_FOLDER = Config.IMG_FOLDER + "/";

	public static ThemeResource getIcon(String icon) {
		return new ThemeResource(WORK_FOLDER + icon);
	}

	public static Resource getIconForSelection(boolean selected) {
		return selected ? FontAwesome.CHECK_SQUARE_O : FontAwesome.SQUARE_O;
	}
}
