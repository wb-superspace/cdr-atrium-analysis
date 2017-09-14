package models.visibilityInteriorsModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.xml.internal.bind.v2.runtime.Location;

import cdr.geometry.primitives.LineSegment;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.Graph;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.Path;
import cdr.graph.methods.paths.SingleSinkShortestPaths;
import cdr.graph.methods.paths.evaluateCost.CostEvaluatorDepth;
import cdr.graph.methods.paths.evaluateCost.euclidean.CostEvaluator3D;
import cdr.graph.methods.paths.evaluateCost.euclidean.CostEvaluatorMetric;
import cdr.graph.model.path.ShortestPathTree;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation.LocationType;
import models.visibilityInteriorsModel.types.path.VisibilityInteriorsPath;

public class VisibilityInteriorsModelTreeBuilder {
	
	public static void buildConnectivityShortestPathTrees(VisibilityInteriorsModel m) {
											
		SingleSinkShortestPaths<Point3D, LineSegment3D> shortestPaths = new SingleSinkShortestPaths<>();
		//CostEvaluatorDepth costEvaluator = new CostEvaluatorDepth();
		//CostEvaluatorMetric costEvaluator = new CostEvaluatorMetric();	
		CostEvaluator3D costEvaluator = new CostEvaluator3D() {
			
			@Override
			public boolean isSymmetric() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public float getStartDistance() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public float evaluateCost(Graph<? extends Point3D, ? extends LineSegment3D> graph, GraphVertex fromParent,
					GraphVertex from, GraphEdge e) {
				// TODO Auto-generated method stub
				
				LineSegment<?> lineSegment = graph.getEdgeData(e) ;
				
				//For superlinks
				if(lineSegment == null) {
					
					return 0 ;
				}
				
				if (
						 m.getConnectivityGraphLocation(from).getTypes().contains(LocationType.ACCESS)) {
					
					if (from.equals(e.getStartVertex())) {
						
						return Float.MAX_VALUE;
					}
				}
				
				return lineSegment.getLength() ;
			}
		};
		
		for (VisibilityInteriorsLocation location : m.getLocations()) {
			
			System.out.println(location);
						
			GraphVertex connectivityVertex = m.getConnectivityGraphVertex(location);
			
			if (connectivityVertex == null) {
				continue;
			}
			
			ShortestPathTree connectivityShortestPathTree =
					shortestPaths.createShortestPathTree(m.getConnectivityGraph(), connectivityVertex, costEvaluator);
										
			for (VisibilityInteriorsLocation target : m.getLocations()) {
								
				if (!target.equals(location) && location.getConnectivityPath(target) == null) {
				
					GraphVertex connectedVertex = m.getConnectivityGraphVertex(target);	
			
					Path graphPath = connectivityShortestPathTree.getShortestPath(connectedVertex);
					
					/*
					 * init
					 */
					
					List<VisibilityInteriorsLocation> pathLocations = new ArrayList<>();
					
					if (graphPath.length() > 1) {
						
						for (GraphVertex pathVertex : graphPath.iterableVertices()) {
							
							VisibilityInteriorsLocation pathLocation = m.getConnectivityGraphLocation(pathVertex);
							
							pathLocations.add(pathLocation);
						}
					}
										
					VisibilityInteriorsPath locationPath = new VisibilityInteriorsPath(pathLocations);
					
//					for (VisibilityInteriorsLocation pathLocation : pathLocations) {
//						pathLocation.addConnectivityFlow(locationPath);
//					}
					
					location.setConnectivityPath(target, locationPath);
					
					/*
					 * reverse
					 */
					
//					List<VisibilityInteriorsLocation> reversePathLocations = pathLocations.subList(0, pathLocations.size());
//					Collections.reverse(reversePathLocations);
//					
//					VisibilityInteriorsPath reverseLocationPath = new VisibilityInteriorsPath(reversePathLocations);
//					
////					for (VisibilityInteriorsLocation reversePathLocation : reversePathLocations) {
////						reversePathLocation.addConnectivityFlow(locationPath);
////					}
//					
//					target.setConnectivityPath(location, reverseLocationPath);
				}
			}			
		}
	}
	
	public static void buildVisibilityShortestPathTrees(VisibilityInteriorsModel m) {
		
		SingleSinkShortestPaths<Point3D, LineSegment3D> shortestPaths = new SingleSinkShortestPaths<>();
		CostEvaluatorDepth costEvaluator = new CostEvaluatorDepth();
		
		for (VisibilityInteriorsLocation location : m.getLocations()) {
			
			System.out.println(location);
			
			GraphVertex visibilityVertex = m.getVisibilityGraphVertex(location);
			
			if (visibilityVertex == null) {
				continue;
			}
			
			ShortestPathTree visibilityShortestPathTree =
					shortestPaths.createShortestPathTree(m.getVisibilityGraph(), visibilityVertex, costEvaluator);
							
			for (VisibilityInteriorsLocation target : m.getLocations()) {
								
				if (!target.equals(location) && location.getVisibilityPath(target) == null) {
					
					GraphVertex visibleVertex = m.getVisibilityGraphVertex(target);
					
					Path graphPath = visibilityShortestPathTree.getShortestPath(visibleVertex);
					
					/*
					 * init
					 */
					
					List<VisibilityInteriorsLocation> pathLocations = new ArrayList<>();
					
					if (graphPath.length() > 1) {
						
						for (GraphVertex pathVertex : graphPath.iterableVertices()) {
							
							VisibilityInteriorsLocation pathLocation = m.getVisibilityGraphLocation(pathVertex);
							
							pathLocations.add(pathLocation);
						}
					}
					
					VisibilityInteriorsPath locationPath = new VisibilityInteriorsPath(pathLocations);
					
//					for (VisibilityInteriorsLocation pathLocation : pathLocations) {
//						pathLocation.addVisibilityFlow(locationPath);
//					}
					
					location.setVisibilityPath(target, locationPath);
					
					/*
					 * reverse
					 */
					
					List<VisibilityInteriorsLocation> reversePathLocations = pathLocations.subList(0, pathLocations.size());
					Collections.reverse(reversePathLocations);
					
					VisibilityInteriorsPath reverseLocationPath = new VisibilityInteriorsPath(reversePathLocations);
					
//					for (VisibilityInteriorsLocation reversePathLocation : reversePathLocations) {
//						reversePathLocation.addVisibilityFlow(locationPath);
//					}
					
					target.setVisibilityPath(location, reverseLocationPath);			
				}
			}			
		}
	}
}
