package models.visibilityInteriorsModel.types.zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.graph.datastructure.euclidean.Graph3D;
import geometry.PolygonsWithHolesGenerator3d;
import graph.GraphUtils;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import topology.MABuilder;

public class VisibilityInteriorsZone {
	
	private final List<VisibilityInteriorsLocation> locations;
	
	private final Graph3D graph;
	
	private final Polygon3D geometry;
		
	protected VisibilityInteriorsZone(List<VisibilityInteriorsLocation> locations, Polygon3D geometry, Graph3D graph) {
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
