package cdr.modified;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.Graphs;
import cdr.graph.datastructure.Path;
import cdr.graph.datastructure.WeightedVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.graph.model.distanceFlow.DistanceFlowModel3D;
import cdr.graph.model.path.ShortestPathTree;



public class ShortestRouteSinksCalculator<A,B> {

	public HashMap<A, List<B>> nearestSinks = new HashMap<>() ;

	public HashMap<A, Float> shortestDistances = new HashMap<>() ; 

	/*
	 * Returns a list of sink objects that are attached to the closest base sink vertex
	 */
	public void calculateShortestRouteSinks(Graph3D baseGraph, 
			List<A> sources, List<B> sinks, 
			GraphVertexAdapter<A> aAdapter, GraphVertexAdapter<B> bAdapter) { 

		//Find vertices closest to souces on base graph ---
		List<WeightedVertex> sourceWeightedVertices = new ArrayList<>() ; 

		HashMap<GraphVertex, List<A>> sourceVertices = new HashMap<>() ; 

		for(A source : sources) {

			GraphVertex sourceGraphVertex = aAdapter.getGeometry(source) ; 

			List<A> closestSources = sourceVertices.get(sourceGraphVertex) ; 

			if(closestSources == null) closestSources = new ArrayList<>() ;
			
			closestSources.add(source) ; 

			sourceVertices.put(sourceGraphVertex, closestSources) ; 

			sourceWeightedVertices.add(Graphs.unitWeightedVertex(sourceGraphVertex)) ;
		}

		//Find vertices closes to sinks on base graph -- 
		
		HashMap<GraphVertex, List<B>> sinkVertices = new HashMap<>() ;

		for(B sink : sinks) {

			GraphVertex sinkGraphVertex = bAdapter.getGeometry(sink) ; 

			List<B> closestSinks = sinkVertices.get(sinkGraphVertex) ; 

			if(closestSinks == null) closestSinks = new ArrayList<>() ; 

			closestSinks.add(sink) ; 
			
			sinkVertices.put(sinkGraphVertex, closestSinks) ; 
		}

		
		//Calculate shortest routes --		
		NearestSinkDistanceFlow3D nearestSinkDistanceFlow = new NearestSinkDistanceFlow3D() ; 

		DistanceFlowModel3D distanceFlowModel = nearestSinkDistanceFlow.nearestSinkDistanceFlowMetric(
			baseGraph, 
			sourceWeightedVertices,
			sinkVertices.keySet(),
			null);
		
		ShortestPathTree shortestPathTree = nearestSinkDistanceFlow.getTree();
		
		nearestSinks.clear() ; 

		shortestDistances.clear() ;

		for(GraphVertex sourceGraphVertex : sourceVertices.keySet()) {

			List<A> representedSources = sourceVertices.get(sourceGraphVertex) ; 		
			Path path = shortestPathTree.getShortestPath(sourceGraphVertex);
			
			for(A representedSource : representedSources) shortestDistances.put(representedSource, distanceFlowModel.getDistance(sourceGraphVertex)) ; 
			
			outerloop:
				for(GraphVertex pathVertex : path.iterableVertices()) {

					List<B> representedSinks = sinkVertices.get(pathVertex) ;

					if(representedSinks != null) {

						for(A representedSource : representedSources) {

							nearestSinks.put(representedSource, representedSinks) ;

							break outerloop ; 
						}
					}
				}
		}
	}
}
