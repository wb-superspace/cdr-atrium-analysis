package cdr.modified;

import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.Graphs;
import cdr.graph.datastructure.WeightedVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.graph.methods.paths.evaluateCost.CostEvaluator;
import cdr.graph.methods.paths.evaluateCost.CostEvaluatorDepth;
import cdr.graph.methods.paths.evaluateCost.euclidean.CostEvaluatorAngular;
import cdr.graph.methods.paths.evaluateCost.euclidean.CostEvaluatorMetric;
import cdr.graph.model.distanceFlow.DistanceFlowModel3D;
import cdr.graph.model.distanceFlow.MapDistanceFlowModel.D3;
import cdr.modified.NearestSinkDistanceFlow;

import java.util.Collection;

public class NearestSinkDistanceFlow3D extends NearestSinkDistanceFlow<Point3D, LineSegment3D> {
	public DistanceFlowModel3D nearestSinkDistanceFlow(Graph3D graph,
			CostEvaluator<? super Point3D, ? super LineSegment3D> evaluator, Collection<WeightedVertex> sources,
			Collection<GraphVertex> sinks, DistanceFlowModel3D result) {
		if (result == null) {
			result = this.createDistanceFlowModel(graph);
		}

		super.nearestSinkDistanceFlow(graph, evaluator, sources, sinks, result);
		return result;
	}

	public DistanceFlowModel3D nearestSinkDistanceFlow(Graph3D graph,
			CostEvaluator<? super Point3D, ? super LineSegment3D> evaluator, Collection<GraphVertex> sinks,
			DistanceFlowModel3D result) {
		return this.nearestSinkDistanceFlow(graph, evaluator,
				Graphs.unitWeightedVertices(graph.iterableVertices(), (Collection) null), sinks, result);
	}

	public DistanceFlowModel3D nearestSinkDistanceFlowMetric(Graph3D graph, Collection<WeightedVertex> sources,
			Collection<GraphVertex> sinks, DistanceFlowModel3D result) {
		return this.nearestSinkDistanceFlow(graph, new CostEvaluatorMetric(), sources, sinks, result);
	}

	public DistanceFlowModel3D nearestSinkDistanceFlowMetric(Graph3D graph, Collection<GraphVertex> sinks,
			DistanceFlowModel3D result) {
		return this.nearestSinkDistanceFlow(graph, new CostEvaluatorMetric(), sinks, result);
	}

	public DistanceFlowModel3D nearestSinkDistanceFlowAngular(Graph3D graph, Collection<WeightedVertex> sources,
			Collection<GraphVertex> sinks, DistanceFlowModel3D result) {
		return this.nearestSinkDistanceFlow(graph, new CostEvaluatorAngular(), sources, sinks, result);
	}

	public DistanceFlowModel3D nearestSinkDistanceFlowAngular(Graph3D graph, Collection<GraphVertex> sinks,
			DistanceFlowModel3D result) {
		return this.nearestSinkDistanceFlow(graph, new CostEvaluatorAngular(), sinks, result);
	}

	public DistanceFlowModel3D nearestSinkDistanceFlowDepth(Graph3D graph, Collection<WeightedVertex> sources,
			Collection<GraphVertex> sinks, DistanceFlowModel3D result) {
		CostEvaluatorDepth evaluator = new CostEvaluatorDepth();
		evaluator.setStartDistance(-1);
		return this.nearestSinkDistanceFlow(graph, evaluator, sources, sinks, result);
	}

	public DistanceFlowModel3D nearestSinkDistanceFlowDepth(Graph3D graph, Collection<GraphVertex> sinks,
			DistanceFlowModel3D result) {
		CostEvaluatorDepth evaluator = new CostEvaluatorDepth();
		evaluator.setStartDistance(-1);
		return this.nearestSinkDistanceFlow(graph, evaluator, sinks, result);
	}

	protected DistanceFlowModel3D createDistanceFlowModel(Graph3D graph) {
		return new D3(graph);
	}
}