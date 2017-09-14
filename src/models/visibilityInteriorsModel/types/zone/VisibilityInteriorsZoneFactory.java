package models.visibilityInteriorsModel.types.zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.graph.datastructure.vertexEdgeGraph.euclidean.VEGraph3D;
import graph.GraphRelaxation;
import graph.GraphUtils;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocationFactory;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation.LocationType;
import topology.MABuilder;

public class VisibilityInteriorsZoneFactory {
	
	private final VisibilityInteriorsModel model;
	private final VisibilityInteriorsLocationFactory locationFactory;
	
	public VisibilityInteriorsZoneFactory(VisibilityInteriorsModel model) {		
		this.model = model;
		this.locationFactory = new VisibilityInteriorsLocationFactory(model);
	}
	
	public VisibilityInteriorsZone createZone(Polygon3D geometry) {
				
		List<VisibilityInteriorsLocation> locations = new ArrayList<>();
		
		geometry.iterablePoints().forEach(point -> {
			
			VisibilityInteriorsLocation location = locationFactory.createLocation(point, LocationType.UNIT, true);
			
			if (location != null) {
				
				locations.add(location);
			}
		});
		
		return new VisibilityInteriorsZone(locations, geometry, new VEGraph3D());
	}
	
	public VisibilityInteriorsZone createCirculationZone(Polygon3D geometry) {
		
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
		
		return new VisibilityInteriorsZone(locations, geometry, graph);
	}
	
	public VisibilityInteriorsZone createLocationZone(VisibilityInteriorsLocation location, Polygon3D geometry) {
		
		Graph3D graph = new VEGraph3D();
		
		graph.addVertex(location);
		
		return new VisibilityInteriorsZone(Arrays.asList(location), geometry, graph);
	}
}
