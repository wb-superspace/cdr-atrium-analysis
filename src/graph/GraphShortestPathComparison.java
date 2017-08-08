package graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import modified.cdr.graph.GraphVertexAdapter;
import modified.cdr.graph.ShortestRouteSinksCalculator;

public class GraphShortestPathComparison  {
		
	public Map<Point3D, Map<Point3D, Float[]>> compareShortestPathDistance(Graph3D g1, Graph3D g2, List<Point3D> sources, List<Point3D> sinks) {
		
		ShortestRouteSinksCalculator<Point3D, Point3D> s1 = new ShortestRouteSinksCalculator<>();
		ShortestRouteSinksCalculator<Point3D, Point3D> s2 = new ShortestRouteSinksCalculator<>();
		Float[] distances;
		
		Map<Point3D, Map<Point3D, Float[]>> values = new HashMap<>();
		
		for (Point3D sink : sinks) {
			
			values.put(sink, new HashMap<>());
			
			GraphVertexAdapter<Point3D> a1 = new GraphVertexAdapter<Point3D>() {
				@Override
				public GraphVertex getGeometry(Point3D location) {			
					return g1.getVertex(location);
				}
			};
			
			GraphVertexAdapter<Point3D> a2 = new GraphVertexAdapter<Point3D>() {
				@Override
				public GraphVertex getGeometry(Point3D location) {				
					return g2.getVertex(location);
				}
			};
			
			s1.calculateShortestRouteSinks(g1,sources, sinks, a1, a1);
			s2.calculateShortestRouteSinks(g2, sources, sinks, a2, a2);
			
			for (Point3D source : sources) {
				
				distances = new Float[2];
				distances[0] = s1.shortestDistances.get(source);
				distances[1] = s2.shortestDistances.get(source);

				values.get(sink).put(source, distances);
			}
		}
		
		return values;
	}
}
