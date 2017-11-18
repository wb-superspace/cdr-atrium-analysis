package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections15.BidiMap;
import cdr.geometry.primitives.Polygon3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.generic.layout.Layout;
import jpantry.models.generic.model.LayoutModel;
import model.connection.Connection;
import model.location.VisibilityInteriorsLocation;
import model.zone.Zone;

public class VisibilityInteriorsModel extends LayoutModel<Layout<LayoutGeometry>> {

	private List<Connection> connections = new ArrayList<>();
	private List<VisibilityInteriorsLocation> locations = new ArrayList<>();
	private List<Zone> zones = new ArrayList<>();

	Graph3D visibilityGraph;
	Graph3D connectivityGraph;

	BidiMap<GraphVertex, VisibilityInteriorsLocation> visibilityGraphLocations;
	BidiMap<GraphVertex, VisibilityInteriorsLocation> connectivityGraphLocations;

	public List<Polygon3D> ref = new ArrayList<>(); // temp

	public Graph3D getVisibilityGraph() {
		return this.visibilityGraph;
	}

	public VisibilityInteriorsLocation getVisibilityGraphLocation(GraphVertex visibilityVertex) {
		return this.visibilityGraphLocations.get(visibilityVertex);
	}

	public GraphVertex getVisibilityGraphVertex(VisibilityInteriorsLocation location) {
		return this.visibilityGraphLocations.getKey(location);
	}

	public Graph3D getConnectivityGraph() {
		return this.connectivityGraph;
	}

	public VisibilityInteriorsLocation getConnectivityGraphLocation(GraphVertex connectivityVertex) {
		return this.connectivityGraphLocations.get(connectivityVertex);
	}

	public GraphVertex getConnectivityGraphVertex(VisibilityInteriorsLocation location) {
		return this.connectivityGraphLocations.getKey(location);
	}

	public VisibilityInteriorsLocation addLocation(VisibilityInteriorsLocation location) {

		for (VisibilityInteriorsLocation other : this.getLocations()) {
			if (other.getAnchor().equals(location.getAnchor())) {
				location = other;
				return location;
			}
		}

		this.locations.add(location);

		return location;
	}

	public List<VisibilityInteriorsLocation> getLocations() {
		return this.locations;
	}

	public List<VisibilityInteriorsLocation> getLocationsModifiable() {
		return this.locations.stream().filter(l -> l.isModifiable() && l.isValid()).collect(Collectors.toList());
	}

	public List<VisibilityInteriorsLocation> getLocationsActive() {
		return this.locations.stream().filter(l -> l.isActive() && l.isValid()).collect(Collectors.toList());
	}

	public List<VisibilityInteriorsLocation> getLocationsTypes(List<VisibilityInteriorsLocation.LocationType> types) {
		return this.locations.stream().filter(l -> !Collections.disjoint(types, l.getTypes()) && l.isValid())
				.collect(Collectors.toList());
	}

	public void addConnection(Connection connection) {
		this.connections.add(connection);
	}

	public List<Connection> getConnections() {
		return this.connections;
	}

	public List<Zone> getZones() {
		return this.zones;
	}

	public void addZone(Zone zone) {
		this.zones.add(zone);
	}
}
