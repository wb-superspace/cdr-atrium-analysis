package cdr.modified;

import cdr.graph.datastructure.Graph;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.Graphs;
import cdr.graph.datastructure.Path;
import cdr.graph.datastructure.WeightedVertex;
import cdr.graph.methods.paths.SingleSinkShortestPaths;
import cdr.graph.methods.paths.evaluateCost.CostEvaluator;
import cdr.graph.methods.paths.evaluateCost.CostEvaluatorDepth;
import cdr.graph.model.distanceFlow.DistanceFlowModel;
import cdr.graph.model.distanceFlow.MapDistanceFlowModel;
import cdr.graph.model.path.ShortestPathTree;
import java.util.Collection;
import java.util.Iterator;

public class NearestSinkDistanceFlow<V, E> {
	private SingleSinkShortestPaths<V, E> shortestPaths = new SingleSinkShortestPaths();
	public ShortestPathTree tree;

	public DistanceFlowModel<V, E> nearestSinkDistanceFlow(Graph<? extends V, ? extends E> graph,
			CostEvaluator<? super V, ? super E> evaluator, Collection<GraphVertex> sinks,
			DistanceFlowModel<V, E> result) {
		return this.nearestSinkDistanceFlow(graph, evaluator,
				Graphs.unitWeightedVertices(graph.iterableVertices(), (Collection) null), sinks, result);
	}

	public DistanceFlowModel<V, E> nearestSinkDistanceFlowDepth(Graph<? extends V, ? extends E> graph,
			Iterable<WeightedVertex> sources, Collection<GraphVertex> sinks, DistanceFlowModel<V, E> result) {
		CostEvaluatorDepth evaluator = new CostEvaluatorDepth();
		evaluator.setStartDistance(-1);
		return this.nearestSinkDistanceFlow(graph, evaluator, sources, sinks, result);
	}

	public DistanceFlowModel<V, E> nearestSinkDistanceFlowDepth(Graph<? extends V, ? extends E> graph,
			Collection<GraphVertex> sinks, DistanceFlowModel<V, E> result) {
		CostEvaluatorDepth evaluator = new CostEvaluatorDepth();
		evaluator.setStartDistance(-1);
		return this.nearestSinkDistanceFlow(graph, evaluator, sinks, result);
	}

	protected DistanceFlowModel<V, E> nearestSinkDistanceFlow(Graph<? extends V, ? extends E> graph,
			CostEvaluator<? super V, ? super E> evaluator, Iterable<WeightedVertex> sources,
			Collection<GraphVertex> sinks, DistanceFlowModel<V, E> result) {
		if (result == null) {
			result = this.createDistanceFlowModel(graph);
		}

		GraphVertex sink = graph.addVertex();
		Iterator arg7 = sinks.iterator();

		while (arg7.hasNext()) {
			GraphVertex source = (GraphVertex) arg7.next();
			graph.connect(sink, source);
		}

		this.tree = this.shortestPaths.createShortestPathTree(graph, sink, evaluator);
		arg7 = sources.iterator();

		while (arg7.hasNext()) {
			WeightedVertex source1 = (WeightedVertex) arg7.next();
			Path path = this.tree.getShortestPath(source1.getGraphVertex());
			result.addPath(path, source1.getWeight(), this.tree);
		}

		result.removeVertex(sink);
		graph.removeVertex(sink);
		return result;
	}

	protected DistanceFlowModel<V, E> createDistanceFlowModel(Graph<? extends V, ? extends E> graph) {
		return new MapDistanceFlowModel(graph);
	}

	public ShortestPathTree getTree() {
		return this.tree;
	}
}