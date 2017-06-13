package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.modified.GraphVertexAdapter;
import cdr.modified.ShortestRouteSinksCalculator;

public class GraphShortestPathAllToAll {

	public Map<Point3D, Map<Point3D, Float>> getShortestDistanceAllToAll(Graph3D graph) {
		
		ShortestRouteSinksCalculator<Point3D, Point3D> calculator = new ShortestRouteSinksCalculator<>();
		
		Map<Point3D, Map<Point3D, Float>> values = new HashMap<>();
		
		List<Point3D> vertexPoints = new ArrayList<>();
		for (GraphVertex vertex : graph.iterableVertices()) {
			vertexPoints.add(graph.getVertexData(vertex));
		}
		
		GraphVertexAdapter<Point3D> a1 = new GraphVertexAdapter<Point3D>() {
			@Override
			public GraphVertex getGeometry(Point3D location) {			
				return graph.getVertex(location);
			}
		};
		
		
		for (Point3D vertexPoint : vertexPoints) {
			
			if (!values.containsKey(vertexPoint)) {
				values.put(vertexPoint, new HashMap<>());
			}
			
			values.get(vertexPoint).put(vertexPoint, 0f);
			
			List<Point3D> sources = new ArrayList<>(vertexPoints);
			List<Point3D> sinks = new ArrayList<>();
			sources.remove(vertexPoint);
			sinks.add(vertexPoint);
			
			calculator.calculateShortestRouteSinks(graph , sources, sinks, a1, a1);
						
			for (Point3D source : sources) {
				
				if (values.containsKey(source) && values.get(source).containsKey(vertexPoint)) {
					continue;
				}
				
				if (!values.containsKey(source)) {
					values.put(source, new HashMap<>());
				}
				
				float distance = calculator.shortestDistances.get(source);

				values.get(vertexPoint).put(source, distance);				
				values.get(source).put(vertexPoint, distance);
			}
		}
		
		return values;
	}
}
