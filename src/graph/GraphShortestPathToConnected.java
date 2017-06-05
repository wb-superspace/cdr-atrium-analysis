package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;

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
}
