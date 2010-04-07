package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;
import java.util.List;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public interface LocationManager {
	public static final Domain LOCATION_EVENT_DOMAIN = DomainFactory.getDomain("location_event");

	public void initialize();

	public void updateLocation(Location location);
	public void removeLocation(Location location);

	public void updateLocation(final LocationInfo mLocationInfo);

	public void updateLocations(Collection<Location> location);
	public void removeLocations(Collection<Location> location);

	public void updateComplete();

	public List<Location> getAllLocations();
	public List<Location> getVisibleLocations();
	public void selectLocation(String locationName);
	public void fitToMap();

	public void reportError(String string, Throwable t);
	
	public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler);

}
