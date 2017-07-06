package graph;

import java.awt.datatransfer.FlavorTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import modified.cdr.GraphVertexAdapter;
import modified.cdr.ShortestRouteSinksCalculator;

public class GraphShortestPathToConnected {

	public Map<Point3D, Map<Point3D, Integer>> getShortestFlowToConnected(Graph3D connectionGraph, Graph3D flowGraph, List<Point3D> sinks) { 
		
		Map<Point3D, Map<Point3D, Integer>> values = new HashMap<>();
		
		for (Point3D sink : sinks) {
		
			values.put(sink, new HashMap<>());
			
			GraphVertex v1 = connectionGraph.getVertex(sink);
			
			List<GraphVertex> visited = new ArrayList<>();
			Queue<GraphVertex> vertexQueue = new LinkedList<>();
			Queue<Integer> depthQueue = new LinkedList<>();
			
			visited.add(flowGraph.getVertex(sink));
			v1.iterableEdges().forEach((e1) -> {
				
				Point3D p1 = connectionGraph.getVertexData(e1.getOther(v1));
				GraphVertex v2 = flowGraph.getVertex(p1);
				
				visited.add(v2);
				vertexQueue.add(v2);
				depthQueue.add(0);
				
				});
					
			while (!vertexQueue.isEmpty()) {
				
				GraphVertex currVertex = vertexQueue.remove();
				Integer currDepth = depthQueue.remove();
				
				values.get(sink).put(flowGraph.getVertexData(currVertex), currDepth);
				
				currVertex.iterableEdges().forEach((e) -> {
					
					GraphVertex testVertex = e.getOther(currVertex);
					
					if (!visited.contains(testVertex)) {
						visited.add(testVertex);
						vertexQueue.add(testVertex);
						depthQueue.add(currDepth+1);
					}
				});
			}
		}
		
		return values;
	}
	
	public Map<Point3D, Map<Point3D, Float[]>> getShortestDistanceToConnected(Graph3D connectionGraph, Graph3D flowGraph, List<Point3D> sinks) {
		
		ShortestRouteSinksCalculator<Point3D, Point3D> s1 = new ShortestRouteSinksCalculator<>();
		
		Map<Point3D, Map<Point3D, Float[]>> values = new HashMap<>();
		
		for (Point3D sink : sinks) {
			
			values.put(sink, new HashMap<>());
			values.get(sink).put(sink, new Float[] {1f,1f});
			
			List<Point3D> connected = new ArrayList<>();
			
			GraphVertex v1 = connectionGraph.getVertex(sink);
			
			v1.iterableEdges().forEach((e1) -> {
				
				Point3D p2 = connectionGraph.getVertexData(e1.getOther(v1));
				GraphVertex v2 = flowGraph.getVertex(p2);
				
				connected.add(p2);
				
			});
			
			List<Point3D> sources = new ArrayList<>();
			
			for (GraphVertex v3 : flowGraph.iterableVertices()) {
				
				Point3D p3 = flowGraph.getVertexData(v3);
				
				if (values.containsKey(p3) && values.get(p3).containsKey(sink)) {
					continue;
				}
				
				if (connected.contains(p3)) {

					values.get(sink).put(p3, new Float[] {1f,1f});
				
				} else {
						
					sources.add(p3);
				}
			}
			
			GraphVertexAdapter<Point3D> a1 = new GraphVertexAdapter<Point3D>() {
				@Override
				public GraphVertex getGeometry(Point3D location) {				
					return flowGraph.getVertex(location);
				}
			};
			
			s1.calculateShortestRouteSinks(flowGraph , sources, connected, a1, a1);
			
			for (Point3D source : sources) {
								
				float flow = s1.shortestDistances.get(source);
				float distance = sink.getDistance(source);
								
				Float[] val = new Float[] {flow, distance};
				
				values.get(sink).put(source, val);	
				
				if (!values.containsKey(source)) {
					values.put(source, new HashMap<>());
				}
				
				values.get(source).put(sink, val);
			}
		}
		
		return values;
	}
}
