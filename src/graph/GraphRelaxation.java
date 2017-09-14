package graph;

import cdr.geometry.primitives.ArrayPoint3D;
import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;

public class GraphRelaxation {

	public static void relaxGraph(Graph3D graph, float t) {
		
		float threshold = 0;
		
		do {
			
			threshold = 0f;
						
			for (GraphVertex vertex : graph.iterableVertices()) {
				
				if (vertex.edgeCount() == 1) {
					continue;
				}
				
				float dx = 0f;
				float dy = 0f;
				float dz = 0f;
				
				for (GraphEdge edge : vertex.iterableEdges()) {
					dx += graph.getVertexData(edge.getOther(vertex)).x() / (float) vertex.edgeCount();
					dy += graph.getVertexData(edge.getOther(vertex)).y() / (float) vertex.edgeCount();
					dz += graph.getVertexData(edge.getOther(vertex)).z() / (float) vertex.edgeCount();
				}
				
				Point3D pt = graph.getVertexData(vertex);
				Point3D dpt = new ArrayPoint3D(dx, dy, dz);
				
				threshold += dpt.getDistance(pt);
				
				graph.moveVertex(vertex, dpt);		
			}
			
				
			System.out.println(threshold);
		
		} while (threshold > t);
	}
	
	public static void relaxGraph(Graph3D graph) {
		
		for (GraphVertex vertex : graph.iterableVertices()) {
			
			if (vertex.edgeCount() == 1) {
				continue;
			}
			
			float dx = 0f;
			float dy = 0f;
			float dz = 0f;
			
			for (GraphEdge edge : vertex.iterableEdges()) {
				dx += graph.getVertexData(edge.getOther(vertex)).x() / (float) vertex.edgeCount();
				dy += graph.getVertexData(edge.getOther(vertex)).y() / (float) vertex.edgeCount();
				dz += graph.getVertexData(edge.getOther(vertex)).z() / (float) vertex.edgeCount();
			}
			
			Point3D dpt = new ArrayPoint3D(dx, dy, dz);
						
			graph.moveVertex(vertex, dpt);		
		}
	}
}
