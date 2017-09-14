package models.visibilityInteriorsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.graph.create.GraphBuilder;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.mesh.datastructure.Edge;
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import geometry.GeometryUtils;
import geometry.Triangulation;
import graph.GraphUtils;
import models.isovistProjectionModel.IsovistProjectionModel25d;
import models.isovistProjectionModel.types.IsovistProjectionFilter;
import models.visibilityInteriorsModel.types.connection.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation.LocationType;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocationFactory;
import models.visibilityInteriorsModel.types.zone.VisibilityInteriorsZoneFactory;

import topology.MABuilder;

public class VisibilityInteriorsModelGraphBuilder {
	
	private static float visibilityDistanceThreshold = 0.1f;

	public static void buildGraphsMABase(VisibilityInteriorsModel m) {
		
		IsovistProjectionModel25d<VisibilityInteriorsLayout, VisibilityInteriorsLocation> im = new IsovistProjectionModel25d<>();
		
		VisibilityInteriorsZoneFactory zoneFactory = new VisibilityInteriorsZoneFactory(m);
		VisibilityInteriorsLocationFactory locationFactory = new VisibilityInteriorsLocationFactory(m);
		
		List<LineSegment3D> connectivityGraphEdges = new ArrayList<>();
		List<LineSegment3D> visibilityGraphEdges = new ArrayList<>();
					
		im.setLayouts(m.getLayouts());
		
		Map<VisibilityInteriorsLayout, List<Polygon3DWithHoles>> buffered = new HashMap<>();
		Map<VisibilityInteriorsLayout, List<Polygon3DWithHoles>> floor = new HashMap<>();
		
		Map<VisibilityInteriorsLayout, Set<Point3D>> connected = new HashMap<>();
		Map<VisibilityInteriorsLayout, Set<Point3D>> unconnected = new HashMap<>();
				
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			connected.put(layout, new HashSet<>());
			unconnected.put(layout, new HashSet<>());
			
			List<Polygon3DWithHoles> iso = layout.buildPolygonsWithHoles(true, true, false, 0f, 0f);
			
			floor.put(layout, iso);
			buffered.put(layout, layout.buildPolygonsWithHoles(true, true, false, -0.1f, 0.1f));
			
			for (Polygon3DWithHoles pgon : iso) {
								
				Graph3D circulationGraph = new MABuilder().generateGraph(pgon);	
				
				GraphUtils.trimGraph(circulationGraph);
				GraphUtils.trimGraph(circulationGraph);
				GraphUtils.reduceVertexByRadius(circulationGraph, 1f);
				
				List<VisibilityInteriorsLocation> circulationLocations = new ArrayList<>();
				
				for (GraphVertex circulationVertex : circulationGraph.iterableVertices()) {
					
					Point3D circulationPoint = circulationGraph.getVertexData(circulationVertex);
					
					VisibilityInteriorsLocation circulationLocation = locationFactory.createLocation(layout, circulationPoint, LocationType.CIRCULATION, false);
					
					m.addLocation(circulationLocation);		
					
					circulationLocations.add(circulationLocation);
					connected.get(layout).add(circulationLocation);
				}
				
				for (GraphEdge circulationEdge : circulationGraph.iterableEdges()) {				
					connectivityGraphEdges.add(circulationGraph.getEdgeData(circulationEdge));
				}
				
				Map<Point3D, Polygon3D> voronoi = Triangulation.voronoi( new ArrayList<>(circulationLocations), pgon);
							
				for (VisibilityInteriorsLocation circulationLocation : circulationLocations) {
					
					Polygon3D geometry = voronoi.get((Point3D)circulationLocation);
					
					if (geometry != null) {
						circulationLocation.setZone(zoneFactory.createLocationZone(circulationLocation, geometry));
					}
				}
			}
		}
				
		for (VisibilityInteriorsConnection connection : m.getConnections()) {
			
			connectivityGraphEdges.add(connection.getGeometry());
			
			unconnected.get(connection.getStartLocation().getLayout()).add(connection.getStartLocation());
			unconnected.get(connection.getEndLocation().getLayout()).add(connection.getEndLocation());
		}
				
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			for (Point3D location : unconnected.get(layout)) {
				
				IsovistLocation catchment = layout.getIsovist(location, buffered.get(layout));
				
				Point3D min = null;
				
				if (catchment != null) {
					
					for (Point3D other : connected.get(layout)) {

						if (catchment.getIsovist().getVisibilityPolygon().isInside(other.getPoint2D(0, 1))) {
							
							if (min == null || location.getDistance(other) < location.getDistance(min)) {
								min = other;
							}
						}
					}
				}
				
				if (min != null) {
					
					connectivityGraphEdges.add(new LineSegment3D(location, min));
					connectivityGraphEdges.add(new LineSegment3D(min, location));
				}
			}
		}
		
		
		// shuts off floorplate edge locations
//		for (VisibilityInteriorsZone zone : this.getZones()) {
//			for (VisibilityInteriorsLocation location : zone.getLocations()) {
//				for (Polygon3DWithHoles f : floor.get(location.getLayout())) {
//					for (Polygon3D contour : f.iterableContours()) {
//						if (GeometryUtils.isPointOnPolygon(location, contour)) {
//							location.setValidity(false);
//						}
//					}
//				}
//			}
//		}
		
		locationLoop:
		for (VisibilityInteriorsLocation location : m.getLocations()) {
						
			System.out.println(location);
								
			if (location.isModifiable() || location.getTypes().contains(LocationType.ACCESS)) {
				
				IsovistLocation catchment = location.getLayout().getIsovist(location, buffered.get(location.getLayout()));
				
				Point3D min = null;
				
				if (catchment != null) {
					
					for (Point3D other : connected.get(location.getLayout())) {

						if (catchment.getIsovist().getVisibilityPolygon().isInside(other.getPoint2D(0, 1))) {
							
							if (min == null || location.getDistance(other) < location.getDistance(min)) {
								min = other;
							}
						}
					}
				} 
				
				if (min != null) {
															
					connectivityGraphEdges.add(new LineSegment3D(location, min));
														
				} else { 
					
					location.setValidity(false);
					continue locationLoop;
				}
			}
						
			im.setLocation(location);
			
			SortedMap<Float, List<Polygon3DWithHoles>> projections = 
					im.getProjectionPolygons(new IsovistProjectionFilter());
						
			for (Map.Entry<Float, List<Polygon3DWithHoles>> projectionEntry : projections.entrySet()) {
				
				if (projectionEntry.getKey() <= location.getLayout().getAnchor().z()) {
					
					Loop : for (VisibilityInteriorsLocation other : m.getLocations()) {
						if (!other.equals(location)) {
													
							if (other.getLayout() == m.getLayout(projectionEntry.getKey())) {
								
								for (Polygon3DWithHoles projection : projectionEntry.getValue()) {
															
									if (projection.getPolygon2DWithHoles(0, 1).isInside(other.getPoint2D(0, 1))) {
										visibilityGraphEdges.add(new LineSegment3D(location, other));
										continue Loop;
									} else {
										
										/*
										 * TODO - this probably needs a better solution 
										 */
										
										for (Polygon3D contour : projection.iterableContours()) {
											for (LineSegment3D edge : contour.iterableEdges()) {							
												if (GeometryUtils.isPointOnBoundedLine(other, edge, visibilityDistanceThreshold)) {
													visibilityGraphEdges.add(new LineSegment3D(location, other));
													continue Loop;
												}
											}	
										}
									}
								}
							} 
						}
					}
				}
			}
		}
						
		m.visibilityGraph = new GraphBuilder().createGraphFromLineSegments(visibilityGraphEdges, true, null);
		m.connectivityGraph = new GraphBuilder().createGraphFromLineSegments(connectivityGraphEdges, true, null);
				
		m.visibilityGraphLocations = new DualHashBidiMap<>();
		m.connectivityGraphLocations = new DualHashBidiMap<>();
		
		for (VisibilityInteriorsLocation location : m.getLocations()) {
									
			GraphVertex visibilityVertex = m.visibilityGraph.findNearestVertex(location, 0.01f);
			GraphVertex connectivityVertex = m.connectivityGraph.findNearestVertex(location, 0.01f);
			
			m.visibilityGraphLocations.put(visibilityVertex, location);
			m.connectivityGraphLocations.put(connectivityVertex, location);
		}
	}
}
