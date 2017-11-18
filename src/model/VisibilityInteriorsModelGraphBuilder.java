package model;

import java.util.ArrayList;
import java.util.Arrays;
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
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import graph.GraphUtils;
import jpantry.geometry.GeometryUtils;
import jpantry.geometry.jts.JtsTriangulationUtils;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.generic.geometry.LayoutGeometryFilter;
import jpantry.models.generic.geometry.LayoutGeometryType;
import jpantry.models.generic.layout.Layout;
import jpantry.models.projection.model.ProjectionModel25d;
import model.connection.Connection;
import model.location.VisibilityInteriorsLocation;
import model.location.VisibilityInteriorsLocationFactory;
import model.location.VisibilityInteriorsLocation.LocationType;
import model.zone.ZoneFactory;
import topology.MABuilder;

public class VisibilityInteriorsModelGraphBuilder {
	
	private static float visibilityDistanceThreshold = 0.1f;

	public static void buildGraphsMABase(VisibilityInteriorsModel m) {
		
		ProjectionModel25d<Layout<LayoutGeometry>, VisibilityInteriorsLocation> im = new ProjectionModel25d<>();
		
		ZoneFactory zoneFactory = new ZoneFactory(m);
		VisibilityInteriorsLocationFactory locationFactory = new VisibilityInteriorsLocationFactory(m);
		
		List<LineSegment3D> connectivityGraphEdges = new ArrayList<>();
		List<LineSegment3D> visibilityGraphEdges = new ArrayList<>();
					
		im.setLayouts(m.getLayouts());
		
		Map<Layout<LayoutGeometry>, List<Polygon3DWithHoles>> buffered = new HashMap<>();
		Map<Layout<LayoutGeometry>, List<Polygon3DWithHoles>> floor = new HashMap<>();
		
		Map<Layout<LayoutGeometry>, Set<Point3D>> connected = new HashMap<>();
		Map<Layout<LayoutGeometry>, Set<Point3D>> unconnected = new HashMap<>();
				
		for (Layout<LayoutGeometry> layout : m.getLayouts().values()) {
			
			connected.put(layout, new HashSet<>());
			unconnected.put(layout, new HashSet<>());
			
			List<Polygon3DWithHoles> iso = layout.buildPolygonsWithHoles(
					Arrays.asList(LayoutGeometryType.FLOOR), 
					Arrays.asList(LayoutGeometryType.WALL, LayoutGeometryType.VOID), 0, 0);
			
			floor.put(layout, iso);
			buffered.put(layout, layout.buildPolygonsWithHoles(
					Arrays.asList(LayoutGeometryType.FLOOR), 
					Arrays.asList(LayoutGeometryType.WALL, LayoutGeometryType.VOID), -0.1f, 0.1f));
			
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
				
				Map<Point3D, Polygon3D> voronoi = JtsTriangulationUtils.voronoi( new ArrayList<>(circulationLocations), pgon);
							
				for (VisibilityInteriorsLocation circulationLocation : circulationLocations) {
					
					Polygon3D geometry = voronoi.get((Point3D)circulationLocation);
					
					if (geometry != null) {
						circulationLocation.setZone(zoneFactory.createLocationZone(circulationLocation, geometry));
					}
				}
			}
		}
				
		for (Connection connection : m.getConnections()) {
			
			connectivityGraphEdges.add(connection.getGeometry());
			
			unconnected.get(connection.getStartLocation().getLayout()).add(connection.getStartLocation());
			unconnected.get(connection.getEndLocation().getLayout()).add(connection.getEndLocation());
		}
				
		for (Layout<LayoutGeometry> layout : m.getLayouts().values()) {
			
			for (Point3D location : unconnected.get(layout)) {
				
				IsovistLocation catchment = layout.computeIsovist(location, buffered.get(layout));
				
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
				
				IsovistLocation catchment = location.getLayout().computeIsovist(location, buffered.get(location.getLayout()));
				
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
					im.getProjectionPolygons(new LayoutGeometryFilter());
						
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
