package evaluations;

import java.awt.datatransfer.FlavorTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.StyleContext.SmallAttributeSet;

import org.apache.commons.math3.ode.FirstOrderConverter;

import com.vividsolutions.jts.algorithm.match.SimilarityMeasure;

import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon2D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.graph.create.GraphBuilder;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.graph.datastructure.vertexEdgeGraph.euclidean.VEGraph3D;
import cdr.interaction.data.insert.PointInsertData;
import cdr.spatialAnalysis.model.isovistModel.IsovistModel;
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import geometry.PolygonsWithHolesGenerator3d;
import graph.GraphShortestPathAllToAll;
import graph.GraphShortestPathComparison;
import graph.GraphShortestPathToConnected;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionLocation;
import models.isovistProjectionModel3d.IsovistProjectionModel;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import templates.Model;

public class VisibilityEvaluationField extends EvaluationField {
		
	String label = "Visibility";
	
	@Override
	public void evaluate(Model m) {

		if (m == null) return;
		
		long startTime = System.nanoTime();	
		this.evaluate((VisibilityInteriorsModel) m);
		long endTime = System.nanoTime();
		float duration = (endTime - startTime) / 1000000000.0f;
		System.out.println("visibility map -> " + duration + "s");
	}
	
	private void evaluate(VisibilityInteriorsModel m) {
		
		IsovistProjectionModel<VisibilityInteriorsLayout> isovistProjectionModel = new IsovistProjectionModel<>();
		IsovistModel isovistModel = new IsovistModel();
		
		Map<VisibilityInteriorsLayout, List<Point3D>> evaluationPoints = new HashMap<>();
		Map<VisibilityInteriorsLayout, List<IsovistProjectionLocation>> evaluationLocations = new HashMap<>();
		
		Graph3D connectivityGraph = new GraphBuilder().createGraphFromLineSegments(m.getConnections(), true, null);
		Graph3D visibilityGraph = new VEGraph3D();
		
		List<GraphVertex> graphVertices = new ArrayList<>();
		Map<Point3D, Integer> graphCatchment = new HashMap<>();
		Map<Point3D, Integer> gridCatchment = new HashMap<>();
		
		connectivityGraph.iterableVertices().forEach(graphVertices :: add);
		
		isovistProjectionModel.setLayouts(m.getLayouts());
		
		/*
		 * get evaluationPoints
		 */
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			evaluationPoints.put(layout, layout.getGrid(this.resolution));
		}
		
		/*
		 * build connectivityGraph
		 */
		
		for (GraphVertex graphVertex : graphVertices) {
			
			Point3D graphPoint = connectivityGraph.getVertexData(graphVertex);
			
			VisibilityInteriorsLayout layout = m.findModelNextMinLayout(graphPoint.z());
			
			if (layout != null) {
				
				List<Point3D> gridPoints = evaluationPoints.get(layout);
				
				List<Polygon3D> isovistPolygons = new ArrayList<>();
				isovistPolygons.addAll(layout.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter()));
				isovistPolygons.addAll(layout.getGeometry(IsovistProjectionGeometryType.WALL, new IsovistProjectionFilter()));
				isovistPolygons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));

				List<Polygon3DWithHoles> isovistPolygonsWithHoles =
						new PolygonsWithHolesGenerator3d().getPolygonsWithHoles(isovistPolygons);
				
				isovistModel.clear();
				isovistModel.setGeometryPolygons3D(isovistPolygonsWithHoles);
				
				IsovistLocation isovistLocation =  isovistModel.addLocation(new PointInsertData(graphPoint), true);
				
				Polygon2D catchment = isovistLocation.getIsovist().getVisibilityPolygon();
				
				for (Point3D gridPoint : gridPoints) {
					
					if (!gridPoint.equals(graphPoint)) {
						
						if (catchment.isInside(gridPoint)) {
							
							GraphVertex gridVertex  = connectivityGraph.getVertex(gridPoint);
							
							if (gridVertex == null) {
								gridVertex = connectivityGraph.addVertex(gridPoint);
							}
							
							connectivityGraph.connect(graphVertex, gridVertex);
						}
					}
				}
			}
		}
				
		for (Map.Entry<VisibilityInteriorsLayout, List<Point3D>> evaluationEntry : evaluationPoints.entrySet()) {
			
			VisibilityInteriorsLayout layout = evaluationEntry.getKey();
			
			List<Polygon3D> isovistPolygons = new ArrayList<>();
			isovistPolygons.addAll(layout.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter()));
			isovistPolygons.addAll(layout.getGeometry(IsovistProjectionGeometryType.WALL, new IsovistProjectionFilter()));
			isovistPolygons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));

			List<Polygon3DWithHoles> isovistPolygonsWithHoles =
					new PolygonsWithHolesGenerator3d().getPolygonsWithHoles(isovistPolygons);
			
			isovistModel.clear();
			isovistModel.setGeometryPolygons3D(isovistPolygonsWithHoles);
			
			for (Point3D evaluationPoint : evaluationEntry.getValue()) {
				
				IsovistLocation isovistLocation =  isovistModel.addLocation(new PointInsertData(evaluationPoint), true);
				
				Polygon2D catchment = isovistLocation.getIsovist().getVisibilityPolygon();
				
				for (Point3D catchmentPoint : evaluationEntry.getValue()) {
					
					if (!evaluationPoint.equals(catchmentPoint)) {
						
						if (catchment.isInside(catchmentPoint)) {
							
							GraphVertex v1 = connectivityGraph.getVertex(evaluationPoint);
							GraphVertex v2 = connectivityGraph.getVertex(catchmentPoint);
							
							if (v1 == null) {
								v1 = connectivityGraph.addVertex(evaluationPoint);
							}
							
							if (v2 == null) {
								v2 = connectivityGraph.addVertex(catchmentPoint);
							}
																
							connectivityGraph.connect(v1, v2);
						}
					}
				}
			}
		}
		
		/*
		 * calcluate graph catchment
		 */
		
		for (GraphVertex graphVertex : graphVertices) {
			
			Point3D graphPoint = connectivityGraph.getVertexData(graphVertex);
			
			VisibilityInteriorsLayout layout = m.findModelNextMinLayout(graphPoint.z());
			
			if (layout != null) {
				
				IsovistProjectionLocation evaluationLocation = new IsovistProjectionLocation(graphPoint);
				
				isovistProjectionModel.setLocation(evaluationLocation);
				
				Map<Float, List<Polygon3DWithHoles>> projectionPolygons = 
						isovistProjectionModel.getProjectionPolygons(new IsovistProjectionFilter());
				
				for (Map.Entry<Float, List<Polygon3DWithHoles>> floorProjections : projectionPolygons.entrySet()) {
					
					VisibilityInteriorsLayout visibleLayout = m.getLayout(floorProjections.getKey());
					
					List<Point3D> catchmentPoints = evaluationPoints.get(visibleLayout);
										
					for (Polygon3DWithHoles catchmentPolygon : floorProjections.getValue()) {
						
						if (floorProjections.getKey() <= graphPoint.z()) {
																			
							for (Point3D catchmentPoint : catchmentPoints) {
								
								if (!catchmentPoint.equals(graphPoint)) {
									
									if (catchmentPolygon.isInside(catchmentPoint)) {
																																							
										if (!graphCatchment.containsKey(catchmentPoint)) {
											graphCatchment.put(catchmentPoint, 0);
										}
										
										graphCatchment.put(catchmentPoint, graphCatchment.get(catchmentPoint) + 1);
									}
								}
							}
						} 
					}
				}
			}
		}
		
		/*
		 * calculate vantage, visibility and grid catchment
		 */
		
		for (float key : m.getLayouts().keySet()) {
			
			VisibilityInteriorsLayout layout = m.getLayout(key);
			
			evaluationLocations.put(layout, new ArrayList<>());
																	
			for (Point3D evaluationPoint : evaluationPoints.get(layout)) {
								
				IsovistProjectionLocation evaluationLocation = new IsovistProjectionLocation(evaluationPoint);
				
				isovistProjectionModel.setLocation(evaluationLocation);
				evaluationLocations.get(layout).add(evaluationLocation);
				
				Map<Float, List<Polygon3DWithHoles>> projectionPolygons = 
						isovistProjectionModel.getProjectionPolygons(new IsovistProjectionFilter());
				
				float vantage = 0f;
				float visibility = 0f;
																
				for (Map.Entry<Float, List<Polygon3DWithHoles>> floorProjections : projectionPolygons.entrySet()) {
															
					VisibilityInteriorsLayout visibleLayout = m.getLayout(floorProjections.getKey());
					
					List<Point3D> gridPoints = evaluationPoints.get(visibleLayout);
										
					for (Polygon3DWithHoles catchment : floorProjections.getValue()) {
						
						if (floorProjections.getKey() <= key) {
							
							vantage += catchment.area();
							
							if (floorProjections.getKey() == key) {
								
								visibility += catchment.area();
							}
							
							for (Point3D gridPoint : gridPoints) {
								
								if (!gridPoint.equals(evaluationPoint)) {
									
									if (catchment.isInside(gridPoint)) {
										
										GraphVertex v1 = visibilityGraph.getVertex(evaluationPoint);
										GraphVertex v2 = visibilityGraph.getVertex(gridPoint);
										
										if (v1 == null) {
											v1 = visibilityGraph.addVertex(evaluationPoint);
										}
										
										if (v2 == null) {
											v2 = visibilityGraph.addVertex(gridPoint);
										}
																			
										visibilityGraph.connect(v1, v2);
										
										if (!gridCatchment.containsKey(gridPoint)) {
											gridCatchment.put(gridPoint, 0);
										}
										
										gridCatchment.put(gridPoint, gridCatchment.get(gridPoint) + 1);
									}
								}
							}
							
						} else {
							
							if (m.getLayouts().lastKey() != key) {
								
								visibility += catchment.area();
							}						
						}
					}
				}
				
				this.setValue("visibility", evaluationPoint.z(), evaluationPoint, visibility + vantage, "m2", ValueType.INTERPOLATE);
				this.setValue("vantage", evaluationPoint.z(), evaluationPoint, vantage, "m2", ValueType.INTERPOLATE);
			}
		}
		
		/*
		 * calculate accessibility and discoverability
		 */
		
		for (List<IsovistProjectionLocation> evaluationlocationList : evaluationLocations.values()) {
						
			for (IsovistProjectionLocation evaluationLocation : evaluationlocationList) {
				
				List<Point3D> sources = new ArrayList<>();
				List<Point3D> sinks = new ArrayList<>(1);
				
				sinks.add(evaluationLocation);
				
				GraphVertex visibilityVertex = visibilityGraph.getVertex(evaluationLocation);
								
				for (GraphEdge visibleEdge : visibilityVertex.iterableEdges()) {					
					GraphVertex visibleVertex = visibleEdge.getOther(visibilityVertex);
					
					Point3D visibleLocation = visibilityGraph.getVertexData(visibleVertex);
					
					sources.add(visibleLocation);
				}
				
				/*
				 * accessibility
				 */
				
				Map<Point3D, Map<Point3D, Float[]>> shortestPathComparison = new GraphShortestPathComparison()
						.compareShortestPathDistance(visibilityGraph, connectivityGraph, sources, sinks);
				
				float accessiblitySum = 0f;
				float accessibilityCount = 0f;
				
				for (Point3D source : shortestPathComparison.get(evaluationLocation).keySet()) {
					
					Float[] result = shortestPathComparison.get(evaluationLocation).get(source);		
					accessiblitySum += result[0] / result[1];
					accessibilityCount++;
				}
				
				float accessibility = accessiblitySum / accessibilityCount;
				
				/*
				 * Discoverability
				 */
				
				Map<Point3D, Map<Point3D, Float[]>> results = new GraphShortestPathToConnected()
						.getShortestDistanceToConnected(visibilityGraph, connectivityGraph, sinks);
				
				float discoverabilitySum = 0f;
				float discoverabilityCount = 0f;

				for (Point3D source : results.get(evaluationLocation).keySet()) {
					
					Float[] result = results.get(evaluationLocation).get(source);	
										
					discoverabilitySum+= result[0];
					discoverabilityCount++;
				}
				
				float discoverability = discoverabilitySum / discoverabilityCount;
				
				this.setValue("accessibility", evaluationLocation.z(), evaluationLocation, accessibility , "", ValueType.INTERPOLATE);
				this.setValue("discoverability", evaluationLocation.z(), evaluationLocation, discoverability, "", ValueType.INTERPOLATE);
				
				System.out.println("accessibility " + evaluationLocation + " " + accessibility);
				System.out.println("discoverability " + evaluationLocation + " " + discoverability);

			}	
		}
		
		for ( Map.Entry<Point3D, Integer> graphCatchmentEntry : graphCatchment.entrySet()) {
			
			Point3D catchmentPoint = graphCatchmentEntry.getKey();
			Float catchmentValue = (float) graphCatchmentEntry.getValue() / (float) graphCatchment.size();
			
			this.setValue("catchment", catchmentPoint.z(), catchmentPoint, catchmentValue , "", ValueType.PERCENTAGE);
		}
		
		for ( Map.Entry<Point3D, Integer> gridCatchmentEntry : gridCatchment.entrySet()) {
			
			Point3D catchmentPoint = gridCatchmentEntry.getKey();
			Float catchmentValue = (float) gridCatchmentEntry.getValue();
			
			this.setValue("perception", catchmentPoint.z(), catchmentPoint, catchmentValue , "", ValueType.INTERPOLATE);
		}
		
		/*
		 * calculate flows
		 */
		
		Map<Point3D, Map<Point3D, Float>> connectivityMap = new GraphShortestPathAllToAll().getShortestDistanceAllToAll(connectivityGraph);
		Map<Point3D, Map<Point3D, Float>> visibilityMap = new GraphShortestPathAllToAll().getShortestDistanceAllToAll(visibilityGraph);
//				
//		for (Point3D visibilityPoint : visibilityMap.keySet()) {
//									
//			/*
//			 * VisibilityDistance
//			 */
//			
//			float visibilityDistance = 0f;
//			float visibilityCount = 0;
//			
//			for (Float value : visibilityMap.get(visibilityPoint).values()) {
//				visibilityDistance += value;
//				visibilityCount ++;
//			}
//			
//			System.out.println(visibilityDistance + " " + visibilityCount);
//			
//			 visibilityDistance /= visibilityCount;
//			
//			this.setValue("visibilityDistance", visibilityPoint.z(), visibilityPoint, visibilityDistance, "", ValueType.INTERPOLATE);
//			
//			/*
//			 * Accessibility
//			 */
//			
//			float accessibilityTotal = 0f;
//			float accessibilityCount = 0f;
//			
//			GraphVertex visibilityVertex = visibilityGraph.getVertex(visibilityPoint);
//			
//			for (GraphEdge visibleEdge : visibilityVertex.iterableEdges()) {					
//				GraphVertex visibleVertex = visibleEdge.getOther(visibilityVertex);
//				
//				Point3D visiblePoint = visibilityGraph.getVertexData(visibleVertex);
//				
//				if (connectivityMap.containsKey(visibilityPoint) && connectivityMap.get(visibilityPoint).containsKey(visiblePoint)) {
//					
//					float visibleDistance = visibilityMap.get(visibilityPoint).get(visiblePoint);
//					float connectedDistance = connectivityMap.get(visibilityPoint).get(visiblePoint);
//					
//					accessibilityTotal += visibleDistance / connectedDistance;
//				}
//				
//				accessibilityCount ++;
//			}
//			
//			accessibilityTotal /= accessibilityCount;
//			
//			this.setValue("accessibility", visibilityPoint.z(), visibilityPoint, accessibilityTotal, "", ValueType.INTERPOLATE);
//			
//		}
		
		for (Point3D connectivityPoint : connectivityMap.keySet()) {
			
			float total = 0f;
			float count = 0;
			
			for (Float value : connectivityMap.get(connectivityPoint).values()) {
				total += value;
				count ++;
			}
			
			System.out.println(total + " " + count);
			
			this.setValue("distance", connectivityPoint.z(), connectivityPoint, total / count, "", ValueType.INTERPOLATE);			
		}
				
		System.out.println("done");
	}
		
	@Override
	public String getLabel() {
		return this.label;
	}
}
