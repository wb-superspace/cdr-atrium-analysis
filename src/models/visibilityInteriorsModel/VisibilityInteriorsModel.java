package models.visibilityInteriorsModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
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
import evaluations.VisibilityInteriorsEvaluationAccessibility;
import evaluations.VisibilityInteriorsEvaluationExposure;
import evaluations.VisibilityInteriorsEvaluationVisibility;
import geometry.GeometryUtils;
import graph.GraphUtils;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionLayout;
import models.isovistProjectionModel3d.IsovistProjectionModel25d;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsZone;
import topology.MABuilder;

public class VisibilityInteriorsModel {
			
	private SortedMap<Float, VisibilityInteriorsLayout> layouts = new TreeMap<>();
	
	private List<VisibilityInteriorsConnection> connections = new ArrayList<>();
	private List<VisibilityInteriorsLocation> locations = new ArrayList<>();
	private List<VisibilityInteriorsZone> zones = new ArrayList<>();
		
	private Graph3D visibilityGraph;
	private Graph3D connectivityGraph;
	
	private BidiMap<GraphVertex, VisibilityInteriorsLocation> visibilityGraphLocations;
	private BidiMap<GraphVertex, VisibilityInteriorsLocation> connectivityGraphLocations;
	
	/*
	 * Temp
	 */
	
	public List<Polygon3D> ref = new ArrayList<>();
	
	private float visibilityDistanceThreshold = 0.1f;
					
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
	
	public void addLocation(VisibilityInteriorsLocation location) {
		
		for (VisibilityInteriorsLocation other : this.getLocations()) {
			if (other.getAnchor().equals(location.getAnchor())) {
				location = other;
				return;
			}
		}
		
		this.locations.add(location);
	}
	
	public List<VisibilityInteriorsLocation> getLocations() {
		return this.locations;
	}
	
	public List<VisibilityInteriorsLocation> getLocationsModifiable() {
		return this.locations.stream().filter(l -> l.isModifiable()).collect(Collectors.toList());
	}
	
	public List<VisibilityInteriorsLocation> getLocationsCatchment() {
		
		Set<VisibilityInteriorsLocation> catchmentLocatons = new HashSet<>();
		
		for (VisibilityInteriorsConnection connection : this.connections) {
			catchmentLocatons.add(connection.getStartLocation());
			catchmentLocatons.add(connection.getEndLocation());
		}
		
		return new ArrayList<>(catchmentLocatons);
	}
	
	public  List<VisibilityInteriorsLocation> getLocationsActive() {
		return this.locations.stream().filter(l -> l.isActive()).collect(Collectors.toList());
	}
	
	public List<VisibilityInteriorsLocation> getLocationsAccess() {
		return this.locations.stream().filter(l -> l.isAccess()).collect(Collectors.toList());
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
		
	public void buildGraphsMABase() {
		
		IsovistProjectionModel25d<VisibilityInteriorsLayout, VisibilityInteriorsLocation> im = new IsovistProjectionModel25d<>();
		
		List<LineSegment3D> connectivityGraphEdges = new ArrayList<>();
		List<LineSegment3D> visibilityGraphEdges = new ArrayList<>();
			
		im.setLayouts(this.layouts);
		
		Map<VisibilityInteriorsLayout, List<Polygon3DWithHoles>> buffered = new HashMap<>();
		
		Map<VisibilityInteriorsLayout, Set<Point3D>> connected = new HashMap<>();
		Map<VisibilityInteriorsLayout, Set<Point3D>> unconnected = new HashMap<>();
				
		for (VisibilityInteriorsLayout layout : this.getLayouts().values()) {
			
			connected.put(layout, new HashSet<>());
			unconnected.put(layout, new HashSet<>());
			
			List<Polygon3DWithHoles> iso = layout.buildPolygonsWithHoles(true, true, false, 0f, 0f);
			
			buffered.put(layout, layout.buildPolygonsWithHoles(true, true, false, -0.1f, 0.1f));
			
			for (Polygon3DWithHoles pgon : iso) {
								
				Graph3D circulationGraph = new MABuilder().generateGraph(pgon);	
				
				GraphUtils.trimGraph(circulationGraph);
				GraphUtils.trimGraph(circulationGraph);
				GraphUtils.reduceVertexByRadius(circulationGraph, 1f);
				
				for (GraphVertex circulationVertex : circulationGraph.iterableVertices()) {
					
					Point3D circulationPoint = circulationGraph.getVertexData(circulationVertex);
					
					VisibilityInteriorsLocation circulationLocation = new VisibilityInteriorsLocation(circulationPoint, layout, false, false);
					
					this.addLocation(circulationLocation);				
					connected.get(layout).add(circulationLocation);
				}
				
				for (GraphEdge circulationEdge : circulationGraph.iterableEdges()) {				
					connectivityGraphEdges.add(circulationGraph.getEdgeData(circulationEdge));
				}
			}
		}
		
		for (VisibilityInteriorsConnection connection : this.getConnections()) {
			
			connectivityGraphEdges.add(connection.getGeometry());
			
			unconnected.get(connection.getStartLocation().getLayout()).add(connection.getStartLocation());
			unconnected.get(connection.getEndLocation().getLayout()).add(connection.getEndLocation());
			
			for (VisibilityInteriorsLocation location: connection.getLocations()) {
		
				VisibilityInteriorsEvaluation catchment = 
						new VisibilityInteriorsEvaluationExposure("access exposure to units (count)", true, true);
				
				catchment.setSinks(Arrays.asList(location));
				
				location.addEvaluation('7', catchment);
			}
		}
		
		for (VisibilityInteriorsLayout layout : this.getLayouts().values()) {
			
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
		
		for (VisibilityInteriorsLocation location : this.locations) {
								
			if (location.isModifiable() || location.isAccess()) {
				
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
					
					LineSegment3D connectivityEdge = new LineSegment3D(location, min);
					
					boolean isIntersectingZone = false;
					
//					loop : for (VisibilityInteriorsZone zone : this.zones) {
//						
//						if (zone.getLocations().contains(location)) {
//							
//							GeometryIntersectionTester tester = new GeometryIntersectionTester();
//							
//							for (LineSegment3D edge : zone.getGeometry().iterableEdges()) {
//								
//								if (!edge.hasVertex(location)) {
//									
//									BoundedLinesIntersection x = tester.intersect2D(connectivityEdge, edge, null);
//									
//									if (x.hasPointIntersection()) {
//										isIntersectingZone = true;
//										break loop;
//									}
//								}
//							}
//						}
//					}
					
					if (!isIntersectingZone) {
						connectivityGraphEdges.add(connectivityEdge);
					}
					
				}
				
				if (location.isAccess()) {
					
					VisibilityInteriorsEvaluation entranceAccessibility =
							new VisibilityInteriorsEvaluationAccessibility("entrance accessibility to units", false, true, false, false);
					entranceAccessibility.setSinks(Arrays.asList(location)); // switch to model.getLocationsAcess for shortestSinks
					
					location.addEvaluation('8', entranceAccessibility);
					
					VisibilityInteriorsEvaluation entranceConnectivity =
							new VisibilityInteriorsEvaluationAccessibility("entrance connectivity to units", true, true, false, false);
					entranceConnectivity.setSinks(Arrays.asList(location));
					
					location.addEvaluation('9', entranceConnectivity);
				}
			
			} else {
				
				VisibilityInteriorsEvaluation visibilityArea =
						new VisibilityInteriorsEvaluationVisibility("circulation visible area (m)", false, false, false);
				visibilityArea.setSinks(Arrays.asList(location));	
					
				VisibilityInteriorsEvaluation exposureUnits =
						new VisibilityInteriorsEvaluationExposure("circulation exposure to units (count)", true, false);
				exposureUnits.setSinks(Arrays.asList(location));
						
				VisibilityInteriorsEvaluation accessibilityEntrance =
						new VisibilityInteriorsEvaluationAccessibility("circulation accessibility to entrances (%)", false, false, false, true);
				accessibilityEntrance.setSinks(Arrays.asList(location));
				
				VisibilityInteriorsEvaluation accessibilityUnits = 
						new VisibilityInteriorsEvaluationAccessibility("circulation accessibility to units (%)", false, true, false, false);
				accessibilityUnits.setSinks(Arrays.asList(location));
				
				VisibilityInteriorsEvaluation accessibilityTotal = 
						new VisibilityInteriorsEvaluationAccessibility("circulation accessibility total (%)", false, false, false, false);
				accessibilityTotal.setSinks(Arrays.asList(location));
				
				VisibilityInteriorsEvaluation connectivityUnits = 
						 new VisibilityInteriorsEvaluationAccessibility("circulation connectivity to units (%)", true, true, false, false);
				connectivityUnits.setSinks(Arrays.asList(location));
					
				location.addEvaluation('1', visibilityArea);
				location.addEvaluation('2', exposureUnits);
				location.addEvaluation('3', accessibilityEntrance);
				location.addEvaluation('4', accessibilityUnits);
				location.addEvaluation('5', accessibilityTotal);
				location.addEvaluation('6', connectivityUnits);
			}
									
			im.setLocation(location);
			
			SortedMap<Float, List<Polygon3DWithHoles>> projections = 
					im.getProjectionPolygons(new IsovistProjectionFilter());
					
			for (Map.Entry<Float, List<Polygon3DWithHoles>> projectionEntry : projections.entrySet()) {
				
				if (projectionEntry.getKey() <= location.getLayout().getAnchor().z()) {
					
					Loop : for (VisibilityInteriorsLocation other : this.locations) {
						if (!other.equals(location)) {
													
							if (other.getLayout() == this.getLayout(projectionEntry.getKey())) {
								
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
						
		this.visibilityGraph = new GraphBuilder().createGraphFromLineSegments(visibilityGraphEdges, true, null);
		this.connectivityGraph = new GraphBuilder().createGraphFromLineSegments(connectivityGraphEdges, true, null);
		
		this.visibilityGraphLocations = new DualHashBidiMap<>();
		this.connectivityGraphLocations = new DualHashBidiMap<>();
		
		for (VisibilityInteriorsLocation location : this.locations) {
									
			GraphVertex visibilityVertex = this.visibilityGraph.findNearestVertex(location, 0.01f);
			GraphVertex connectivityVertex = this.connectivityGraph.findNearestVertex(location, 0.01f);
			
			this.visibilityGraphLocations.put(visibilityVertex, location);
			this.connectivityGraphLocations.put(connectivityVertex, location);
		}
	}
}


