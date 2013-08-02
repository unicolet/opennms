/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.datacollection;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.datacollection.MibObj;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * The MIB Object Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibObjField extends CustomField<ArrayList<MibObj>> implements Button.ClickListener {
	
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3665919460707298011L;

    /** The Table. */
    private final Table table = new Table();

    /** The Container. */
    private final BeanContainer<String,MibObj> container = new BeanContainer<String,MibObj>(MibObj.class);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add = new Button("Add", this);

    /** The delete button. */
    private final Button delete = new Button("Delete", this);

    /**
     * Instantiates a new MIB object field.
     *
     * @param resourceTypes the available resource types
     */
    public MibObjField(final List<String> resourceTypes) {
        container.setBeanIdProperty("oid");
        table.setContainerDataSource(container);
        table.addStyleName("light");
        table.setVisibleColumns(new Object[]{"oid", "instance", "alias", "type"});
        table.setColumnHeader("oid", "OID");
        table.setColumnHeader("instance", "Instance");
        table.setColumnHeader("alias", "Alias");
        table.setColumnHeader("type", "Type");
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("250px");
        table.setWidth("100%");
        table.setTableFieldFactory(new MibObjFieldFactory(resourceTypes));

        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        setBuffered(true);
        setValidationVisible(true);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<MibObj>> getType() {
        return (Class<ArrayList<MibObj>>) new ArrayList<MibObj>().getClass();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof List<?>) {
            List<MibObj> beans = (List<MibObj>) value;
            container.removeAllItems();
            container.addAll(beans);
            table.setPageLength(beans.size());
        } else {
            throw new ConversionException("Invalid type");
        }
        super.setPropertyDataSource(newDataSource);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getValue()
     */
    @Override
    public ArrayList<MibObj> getValue() {
        ArrayList<MibObj> beans = new ArrayList<MibObj>(); 
        for (Object itemId: container.getItemIds()) {
            beans.add(container.getItem(itemId).getBean());
        }
        return beans;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        table.setEditable(!readOnly);
        toolbar.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button btn = event.getButton();
        if (btn == add) {
            addHandler();
        }
        if (btn == delete) {
            deleteHandler();
        }
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#isValid()
     */
    @Override
    public boolean isValid() {
        return table.isValid(); // FIXME This is not working
    }

    /**
     * Adds the handler.
     */
    private void addHandler() {
        MibObj obj = new MibObj();
        obj.setOid("1.1.1.1");
        container.addBean(obj);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a MIB Object from the table.");
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to remove the selected MIB Object?\nThis action cannot be undone.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        table.removeItem(itemId);
                    }
                }
            });
        }
    }

}