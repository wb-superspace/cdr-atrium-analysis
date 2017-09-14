package models.visibilityInteriorsModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.geometry.toolkit.intersect.GeometryIntersectionTester;
import cdr.geometry.toolkit.intersect.results.BoundedLinesIntersection;
import cdr.graph.create.GraphBuilder;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import evaluations.VisibilityInteriorsEvaluation;
import evaluations.VisibilityInteriorsEvaluation.EvaluationType;
import geometry.Triangulation;
import geometry.GeometryUtils;
import graph.GraphUtils;
import models.isovistProjectionModel.IsovistProjectionModel25d;
import models.isovistProjectionModel.types.IsovistProjectionFilter;
import models.isovistProjectionModel.types.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.connection.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation.LocationType;
import models.visibilityInteriorsModel.types.zone.VisibilityInteriorsZone;
import models.visibilityInteriorsModel.types.zone.VisibilityInteriorsZoneFactory;
import search.LookupGridManager;
import topology.MABuilder;

public class VisibilityInteriorsModel {
			
	private SortedMap<Float, VisibilityInteriorsLayout> layouts = new TreeMap<>();
	
	private List<VisibilityInteriorsConnection> connections = new ArrayList<>();
	private List<VisibilityInteriorsLocation> locations = new ArrayList<>();
	private List<VisibilityInteriorsZone> zones = new ArrayList<>();
		
	Graph3D visibilityGraph;
	Graph3D connectivityGraph;
	
	BidiMap<GraphVertex, VisibilityInteriorsLocation> visibilityGraphLocations;
	BidiMap<GraphVertex, VisibilityInteriorsLocation> connectivityGraphLocations;
	
	public List<Polygon3D> ref = new ArrayList<>(); //temp
					
	public VisibilityInteriorsLayout getLayout(Float key) {
		return this.layouts.get(key);
	}
	
	public SortedMap<Float, VisibilityInteriorsLayout> getLayouts() {
		return this.layouts;
	}
	
	public VisibilityInteriorsLayout findModelNextMinLayout(float z) {
		
		VisibilityInteriorsLayout layout = null;
		
		for (Map.Entry<Float, VisibilityInteriorsLayout> entry : this.getLayouts().entrySet()) {
			
			if (entry.getKey() > z) {
				break;
			}
			
			layout = entry.getValue();
		}
		
		return layout;
	}
	
	public List<VisibilityInteriorsLayout> findModelBoundedLayouts(float zMin, float zMax) {
		
		List<VisibilityInteriorsLayout> layouts = new ArrayList<>();
		
		for (Map.Entry<Float, VisibilityInteriorsLayout> entry : this.getLayouts().entrySet()) {
			
			if (entry.getKey() < zMin) {
				continue;
			} else if (entry.getKey() > zMax) {
				break;
			}
			
			layouts.add(entry.getValue());
		}
				
		return layouts;
	}
				
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
	
	public VisibilityInteriorsLocation getConnectivityGraphLocation (GraphVertex connectivityVertex) {
		return this.connectivityGraphLocations.get(connectivityVertex);
	}
	
	public GraphVertex getConnectivityGraphVertex (VisibilityInteriorsLocation location) {
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
		
	public  List<VisibilityInteriorsLocation> getLocationsActive() {
		return this.locations.stream().filter(l -> l.isActive() && l.isValid()).collect(Collectors.toList());
	}
	
	public List<VisibilityInteriorsLocation> getLocationsTypes(List<VisibilityInteriorsLocation.LocationType> types) {
		return this.locations.stream().filter(l -> !Collections.disjoint(types, l.getTypes()) && l.isValid()).collect(Collectors.toList());
	}
		
	public void addConnection(VisibilityInteriorsConnection connection) {
		this.connections.add(connection);
	}
	
	public List<VisibilityInteriorsConnection> getConnections() {
		return this.connections;
	}
	
	public void addZone(VisibilityInteriorsZone zone) {
		this.zones.add(zone);
	}
	
	public List<VisibilityInteriorsZone> getZones() {
		return this.zones;
	}
}


