package model.zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.graph.datastructure.vertexEdgeGraph.euclidean.VEGraph3D;
import graph.GraphUtils;
import jpantry.graph.GraphRelaxation;
import model.VisibilityInteriorsModel;
import model.location.VisibilityInteriorsLocation;
import model.location.VisibilityInteriorsLocationFactory;
import model.location.VisibilityInteriorsLocation.LocationType;
import topology.MABuilder;

public class ZoneFactory {
	
	private final VisibilityInteriorsModel model;
	private final VisibilityInteriorsLocationFactory locationFactory;
	
	public ZoneFactory(VisibilityInteriorsModel model) {		
		this.model = model;
		this.locationFactory = new VisibilityInteriorsLocationFactory(model);
	}
	
	public Zone createZone(Polygon3D geometry) {
				
		List<VisibilityInteriorsLocation> locations = new ArrayList<>();
		
		geometry.iterablePoints().forEach(point -> {
			
			VisibilityInteriorsLocation location = locationFactory.createLocation(point, LocationType.UNIT, true);
			
			if (location != null) {
				
				locations.add(location);
			}
		});
		
		return new Zone(locations, geometry, new VEGraph3D());
	}
	
	public Zone createCirculationZone(Polygon3D geometry) {
		
		Polygon3DWithHoles p = new Polygon3DWithHoles();
		p.setOuterContour(geometry);
		
		Graph3D graph = new MABuilder().generateGraph(p);
		
		//GraphUtils.trimGraph(graph);
		GraphUtils.reduceVertexByRadius(graph, 1f);
		GraphRelaxation.relaxGraph(graph, 0.1f);
		
		List<VisibilityInteriorsLocation> locations = new ArrayList<>();
				
		graph.iterableVertices().forEach(vertex -> {
									
			VisibilityInteriorsLocation location = locationFactory.createLocation(graph.getVertexData(vertex), LocationType.UNIT, true);
				
			if (location != null) {
				
				locations.add(location);
			}
		});
		
		return new Zone(locations, geometry, graph);
	}
	
	public Zone createLocationZone(VisibilityInteriorsLocation location, Polygon3D geometry) {
		
		Graph3D graph = new VEGraph3D();
		
		graph.addVertex(location);
		
		return new Zone(Arrays.asList(location), geometry, graph);
	}
}
