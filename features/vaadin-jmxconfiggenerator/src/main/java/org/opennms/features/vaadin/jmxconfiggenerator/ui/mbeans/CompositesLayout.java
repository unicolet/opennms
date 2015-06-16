package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ModelChangeListener;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UIHelper;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

class CompositesLayout extends VerticalLayout implements ViewStateChangedListener, ModelChangeListener<Mbean> {

    private final TabSheet tabSheet = new TabSheet();
    private final MBeansController controller;
    private int selectedCompositesTabPosition;

	public CompositesLayout(MBeansController controller) {
        this.controller = controller;
        setSizeFull();
        setSpacing(false);
        setMargin(false);
        tabSheet.setSizeFull();
        addComponent(tabSheet);
    }

    @Override
    public void viewStateChanged(ViewStateChangedEvent event) {
		selectedCompositesTabPosition = UIHelper.enableTabs(tabSheet, event, selectedCompositesTabPosition);
		if (tabSheet.getSelectedTab() == null) return;
		((CompositeTabLayout) tabSheet.getSelectedTab()).viewStateChanged((event)); //forwared
    }

    @Override
    public void modelChanged(Mbean newModel) {
        tabSheet.removeAllComponents();
        int no = 1;
        for (CompAttrib attrib : newModel.getCompAttrib()) {
            final String tabLabel = String.format("#%d %s", no++, attrib.getName());
            final CompositeTabLayout tabContent = new CompositeTabLayout(getCompositeForm(newModel, attrib), getCompAttribTable(newModel, attrib));
            tabSheet.addTab(tabContent, tabLabel);
        }
    }

    private NameEditForm getCompositeForm(final Mbean mbean, final CompAttrib compAttrib) {
        NameEditForm form = new NameEditForm(controller, new FormParameter() {
            @Override
			public boolean hasFooter() {
				return false;
			}

			@Override
			public String getCaption() {
				return null;
			}

			@Override
			public String getEditablePropertyName() {
				return "name";
			}

			@Override
			public String getNonEditablePropertyName() {
				return "alias";
			}

			@Override
			public Object[] getVisiblePropertieNames() {
				return new Object[]{"selected", getNonEditablePropertyName(), getEditablePropertyName()};
			}

			@Override
			public EditControls.Callback getAdditionalCallback() {
				return null;
			}
        });
        Item item = controller.getCompositeAttributeContainer(mbean).getItem(compAttrib);
        form.setItemDataSource(item);
        return form;
    }

    private Table getCompAttribTable(final Mbean mbean, final CompAttrib attrib) {
        AttributesTable memberTable = new AttributesTable(controller, new MBeansController.Callback() {
            @Override
            public Container getContainer() {
                return controller.getCompositeMemberContainer(attrib);
            }
        });
        memberTable.modelChanged(mbean);
        return memberTable;
    }

    private class CompositeTabLayout extends VerticalLayout implements Property.ReadOnlyStatusChangeNotifier, EditControls.Callback, ViewStateChangedListener {

        private final NameEditForm compositeForm;
        private final Table compositeTable;
        private final EditControls.FormButtonHandler<NameEditForm> formButtonHandler;
        private final EditControls.TableButtonHandler<Table> tableButtonHandler;
        private final EditControls<AbstractField<?>> footer;

        private CompositeTabLayout(NameEditForm compositeForm, Table compositeTable) {
            this.compositeForm = compositeForm;
            this.compositeTable = compositeTable;
            formButtonHandler = new EditControls.FormButtonHandler<NameEditForm>(compositeForm);
            tableButtonHandler = new EditControls.TableButtonHandler<Table>(compositeTable);
            footer = new EditControls<AbstractField<?>>(this, new EditControls.ButtonHandler<AbstractField<?>>() {
                @Override
                public void handleSave() {
                    if (formButtonHandler.getOuter().isValid() && tableButtonHandler.getOuter().isValid()) {
                        formButtonHandler.handleSave();
                        tableButtonHandler.handleSave();
                    } else {
                        UIHelper.showValidationError("There are some errors on this view. Please fix them first");
                    }
                }

                @Override
                public void handleCancel() {
                    formButtonHandler.handleCancel();
                    tableButtonHandler.handleCancel();
                }

                @Override
                public void handleEdit() {
                    formButtonHandler.handleEdit();
                    tableButtonHandler.handleEdit();
                }

                @Override
                public AbstractField getOuter() {
                    return null;
                }
			});
            setSizeFull();
            setSpacing(false);
            setReadOnly(true);
            addComponent(footer);
            addComponent(compositeForm);
            addComponent(compositeTable);
            addFooterHooks(footer);
            setExpandRatio(compositeTable, 1);
        }

        @Override
        public void addListener(Property.ReadOnlyStatusChangeListener listener) {
            addReadOnlyStatusChangeListener(listener);
        }

        @Override
        public void addReadOnlyStatusChangeListener(Property.ReadOnlyStatusChangeListener listener) {
            compositeForm.addListener(listener);
            compositeTable.addListener(listener);
        }

        @Override
        public void removeListener(Property.ReadOnlyStatusChangeListener listener) {
            removeReadOnlyStatusChangeListener(listener);
        }

        @Override
        public void removeReadOnlyStatusChangeListener(Property.ReadOnlyStatusChangeListener listener) {
            compositeForm.removeListener(listener);
            compositeTable.removeListener(listener);
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            compositeForm.setReadOnly(readOnly);
            compositeTable.setReadOnly(readOnly);

        }

        @Override
        public void callback(EditControls.ButtonType type, Component outer) {
            if (type == EditControls.ButtonType.edit) {
                controller.fireViewStateChanged(ViewState.Edit, outer);
            }
            if (type == EditControls.ButtonType.cancel) {
                controller.fireViewStateChanged(ViewState.LeafSelected, outer);
            }
            if (type == EditControls.ButtonType.save && compositeForm.isValid() && compositeTable.isValid()) {
                controller.fireViewStateChanged(ViewState.LeafSelected, CompositeTabLayout.this);
            }
        }

		private void addFooterHooks(final EditControls<?> footer) {
            footer.addSaveHook(this);
            footer.addCancelHook(this);
            footer.addEditHook(this);
        }

        @Override
        public void viewStateChanged(ViewStateChangedEvent event) {
            switch (event.getNewState()) {
                case Init:
                case LeafSelected:
                    setEnabled(true);
                    footer.setVisible(true);
                    break;
                case NonLeafSelected:
                case Edit:
                    setEnabled(event.getSource() == this);
                    footer.setVisible(event.getSource() == this);
                    break;
            }
        }
    }
}
