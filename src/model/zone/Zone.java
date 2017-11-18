package model.zone;

import java.util.List;

import cdr.geometry.primitives.Polygon3D;
import cdr.graph.datastructure.euclidean.Graph3D;
import model.location.VisibilityInteriorsLocation;

public class Zone {
	
	private final List<VisibilityInteriorsLocation> locations;
	
	private final Graph3D graph;
	
	private final Polygon3D geometry;
		
	protected Zone(List<VisibilityInteriorsLocation> locations, Polygon3D geometry, Graph3D graph) {
		this.locations = locations;
		this.geometry = geometry;
		this.graph = graph;
	}
	
	public Polygon3D getGeometry() {
		return this.geometry;
	}
	
	public List<VisibilityInteriorsLocation> getLocations() {
		return this.locations;
	}
	
	public Graph3D getGraph() {
		return this.graph;
	}
}
