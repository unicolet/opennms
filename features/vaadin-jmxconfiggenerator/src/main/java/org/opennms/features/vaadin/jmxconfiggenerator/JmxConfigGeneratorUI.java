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

package org.opennms.features.vaadin.jmxconfiggenerator;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import org.opennms.features.jmxconfiggenerator.Starter;
import org.opennms.features.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.features.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.features.jmxconfiggenerator.graphs.Report;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ModelChangeListener;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ServiceConfig;
import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ConfigView;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.MBeansView;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ConfigResultView;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.HeaderPanel;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.IntroductionView;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ModelChangeRegistry;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ProgressWindow;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UiState;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Collection;

@Theme(Config.STYLE_NAME)
@Title("JmxConfigGenerator UI Tool")
@SuppressWarnings("serial")
public class JmxConfigGeneratorUI extends UI {

    private static final Logger LOG = LoggerFactory.getLogger(JmxConfigGeneratorUI.class);

	/**
	 * The Header panel which holds the steps which are necessary to complete
	 * the configuration for a new service to get collected.
	 * 
	 */
	private HeaderPanel headerPanel;

	/**
	 * The "content" panel which shows the view for the currently selected step
	 * of the configuration process.
	 */
	private Panel contentPanel;

	private ProgressWindow progressWindow;

	private UiModel model = new UiModel();
//	private ModelChangeRegistry modelChangeRegistry = new ModelChangeRegistry();
	private Navigator navigator;

	@Override
	protected void init(VaadinRequest request) {
		initHeaderPanel();
		initContentPanel();
		initMainLayout();
		initNavigator();

//		registerListener(UiModel.class, this);
		updateView(UiState.IntroductionView);
	}

	// TODO MVR implement
	private void initNavigator() {
		navigator = new Navigator(this, contentPanel);
		// common views
		navigator.addView(UiState.IntroductionView.name(), new IntroductionView(this));
		navigator.addView(UiState.ServiceConfigurationView.name(), new ConfigView(this));
		navigator.addView(UiState.MbeansView.name(), new MBeansView(this));
		navigator.addView(UiState.ResultView.name(), new ConfigResultView(this));

		// "working views" need to be simulated, they do not actually ecist, but we need them for navigation
		navigator.addView(UiState.MbeansDetection.name(), new Navigator.EmptyView());
		navigator.addView(UiState.ResultConfigGeneration.name(), new Navigator.EmptyView());


		// We need to hook into the "view change" process to prevent changing to the "working views"
		// Instead we trigger a long running task and show a "please wait" window.
		navigator.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				removeWindow(getProgressWindow());
				headerPanel.enter(event);

				final UiState uiState = UiState.valueOf(event.getViewName());
				if (UiState.MbeansDetection == uiState) {
					showProgressWindow(uiState.getDescription() + ".\n\n This may take a while ...");
					new DetectMBeansWorkerThread(((JmxConfigGeneratorUI) UI.getCurrent()).getUiModel().getServiceConfig()).start();
					return false;
				}
				if (UiState.ResultConfigGeneration == uiState) {
					showProgressWindow(uiState.getDescription() +  ".\n\n This may take a while ...");
					new CreateOutputWorkerThread().start();
					return false;
				}
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {

			}
		});
	}

	private void initHeaderPanel() {
		headerPanel = new HeaderPanel();
//		registerListener(UiState.class, headerPanel);
	}

	// the Main panel holds all views such as Config view, mbeans view, etc.
	private void initContentPanel() {
		contentPanel = new Panel();
		contentPanel.setContent(new VerticalLayout());
		contentPanel.getContent().setSizeFull();
		contentPanel.setSizeFull();
	}

	/**
	 * Creates the main window and adds the header, main and button panels to
	 * it.
	 */
	private void initMainLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(headerPanel);
		layout.addComponent(contentPanel);
		// content Panel should use most of the space
		layout.setExpandRatio(contentPanel, 1);
		setContent(layout);
	}

	public void updateView(UiState uiState) {
		navigator.navigateTo(uiState.name());
//		switch (uiState) {
//			case IntroductionView:
//			case ServiceConfigurationView:
//			case MbeansView:
//			case ResultView:
//				setContentPanelComponent(getView(uiState));
//				notifyObservers(UiModel.class, model);
//				break;
//			case MbeansDetection:
//				showProgressWindow(uiState.getDescription() + ".\n\n This may take a while ...");
//				new DetectMBeansWorkerThread().start();
//				break;
//			case ResultConfigGeneration:
//				showProgressWindow(uiState.getDescription() +  ".\n\n This may take a while ...");
//				new CreateOutputWorkerThread().start();
//				break;
//		}
//		notifyObservers(UiState.class,  uiState);
	}

//	private Component createView(UiState uiState, JmxConfigGeneratorUI app) {
//		Component component = null;
//		switch (uiState) {
//			case IntroductionView:
//				component = new IntroductionView(app);
//				break;
//			case ServiceConfigurationView:
//				component = new ConfigView(app);
//				registerListener(UiModel.class, (ModelChangeListener<?>) component);
//				break;
//			case MbeansView:
//				component = new MBeansView(app);
//				registerListener(UiModel.class, (ModelChangeListener<?>) component);
//				break;
//			case ResultView:
//				component = new ConfigResultView(app);
//				registerListener(UiModel.class, (ModelChangeListener<?>) component);
//				break;
//		}
//		return component;
//	}

//	private Component getView(UiState uiState) {
//		if (viewCache.get(uiState) == null) {
//			Component component = createView(uiState, JmxConfigGeneratorUI.this);
//			if (component == null) return null; // no "real" view
//			viewCache.put(uiState, component);
//		}
//		return viewCache.get(uiState);
//	}

	private ProgressWindow getProgressWindow() {
		if (progressWindow == null) {
			progressWindow = new ProgressWindow();
		}
		return progressWindow;
	}

	private void showProgressWindow(String label) {
		getProgressWindow().setLabelText(label);
		addWindow(getProgressWindow());
	}

//	private void registerListener(Class<?> aClass, ModelChangeListener<?> listener) {
//		modelChangeRegistry.registerListener(aClass, listener);
//	}

//	private void notifyObservers(Class<?> aClass, Object object) {
//		modelChangeRegistry.notifyObservers(aClass, object);
//	}
	
    private class DetectMBeansWorkerThread extends Thread {

		private ServiceConfig config;

		private DetectMBeansWorkerThread(ServiceConfig config)  {
			this.config = config;
		}

		@Override
        public void run() {
            try {
                // TODO loading of the dictionary should not be done via the Starter class and not in a static way!
                JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator();
                JMXServiceURL jmxServiceURL = jmxConfigGenerator.getJmxServiceURL(config.isJmxmp(), config.getHost(), config.getPort());
                JMXConnector connector = jmxConfigGenerator.getJmxConnector(config.getUser(), config.getPassword(), jmxServiceURL);
                JmxDatacollectionConfig generateJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(connector.getMBeanServerConnection(), "anyservice", !config.isSkipDefaultVM(), config.isRunWritableMBeans(), Starter.loadInternalDictionary());
                connector.close();

				UI.getCurrent().access(new Runnable() {
					@Override
					public void run() {
						model.setRawModel(generateJmxConfigModel);
						updateView(UiState.MbeansView);
					}
				});
			} catch (IOException | SecurityException ex) {
				handleError(ex);
			}
        }

        private void handleError(final Exception ex) {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					//TODO MVR logging?
					LOG.error("TODO", ex);
					Notification.show("Connection error", "An error occured during connection jmx service. Please verify connection settings.<br/><br/>" + ex.getMessage(), Type.ERROR_MESSAGE);
				}
			});
        }
    }

	private class CreateOutputWorkerThread extends Thread {

		@Override
		public void run() {
			// create snmp-graph.properties

			GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator();
			Collection<Report> reports = new JmxConfigReader().generateReportsByJmxDatacollectionConfig(model.getOutputConfig());

			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					try {
						model.setSnmpGraphProperties(graphConfigGenerator.generateSnmpGraph(reports));
						model.updateOutput();
						updateView(UiState.ResultView);
					} catch (IOException ex) {
						model.setSnmpGraphProperties(ex.getMessage()); // TODO handle Errors in UI
						LOG.error("SNMP Graph-Properties couldn't be created.", ex);
					}
				}
			});



		}
	}

	public UiModel getUiModel() {
		return model;
	}
}
