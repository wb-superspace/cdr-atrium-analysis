package rendering;

import java.util.stream.Collectors;

import javax.media.opengl.GL2;

import cdr.joglFramework.camera.GLCamera;
import evaluations.VisibilityInteriorsEvaluation;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.generic.layout.Layout;
import jpantry.models.generic.model.LayoutModel;
import jpantry.models.generic.rendering.LayoutModelRenderer;
import model.VisibilityInteriorsModel;

public class VisibilityInteriorsModelRenderer {

	private VisibilityInteriorsLocationRenderer locationRenderer = new VisibilityInteriorsLocationRenderer();
	private LayoutModelRenderer<LayoutModel<Layout<LayoutGeometry>>> modelRenderer = new LayoutModelRenderer<>();

	private VisibilityInteriorsEvaluation evaluation = null;

	public boolean renderProjectionPolygons = false;
	public boolean renderProjectionPolyhedra = false;

	public boolean renderVisibilityCatchmentPolygons = false;

	public boolean renderEvaluation = true;
	public boolean renderEvaluationLabels = false;

	public boolean renderEvaluationZones = true;

	public boolean renderEvaluationNodeSinks = true;
	public boolean renderEvaluationNodeSinkLabels = true;
	public boolean renderEvaluationNodeSources = true;
	public boolean renderEvaluationNodeSourceLabels = true;

	public boolean renderEvaluationEdges = true;
	public boolean renderEvaluationVisibilityLines = false;

	public boolean renderPlan = false;
	public boolean renderMesh = true;
	public boolean renderTransparent = false;

	public boolean renderConnections = true;

	public void update(VisibilityInteriorsEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	public void renderFill(GL2 gl, VisibilityInteriorsModel m) {

		if (m == null) {
			return;
		}

		if (!renderPlan) {

			if (renderMesh && !renderTransparent) {
				modelRenderer.renderMeshesFill(gl, m);
			}

		} else {

			modelRenderer.renderFloorsFill(gl, m);
			modelRenderer.renderWallsFill(gl, m);
		}
	}

	public void renderLines(GL2 gl, VisibilityInteriorsModel m) {

		if (m == null) {
			return;
		}

		if (!renderPlan) {

			if (renderMesh && renderTransparent) {
				modelRenderer.renderMeshesFill(gl, m);
				modelRenderer.renderVoidsFill(gl, m);
			}

			if (renderTransparent) {
				modelRenderer.renderLinesThin(gl, m);

			} else {

				if (!renderEvaluation || evaluation == null) {

					modelRenderer.renderMeshesLines(gl, m);
					modelRenderer.renderLines(gl, m);
				}
			}

		} else {
			modelRenderer.renderLines(gl, m);
		}

		if (renderConnections) {
			modelRenderer.renderLineSegments3DThick(gl, m.getConnections().stream().map(c -> c.getGeometry()).collect(Collectors.toList()));
		}

		if (renderEvaluation && evaluation != null) {

			if (renderEvaluationZones) {
				locationRenderer.renderEvaluationSinkLocationsZonesLines(gl, evaluation);
				locationRenderer.renderEvaluationSourceLocationsZonesLines(gl, evaluation);
				locationRenderer.renderEvaluationSourceLocationsZonesFlow(gl, evaluation);
				locationRenderer.renderEvaluationSinkLocationsZonesFlow(gl, evaluation);
				locationRenderer.renderEvaluationZones(gl, evaluation, m);
			}

			if (renderProjectionPolygons) {
				modelRenderer.renderFloorsFill(gl, m, new float[] { 0, 0, 1 });
				modelRenderer.renderWallsFill(gl, m);
				locationRenderer.renderLocationsProjectionPolygons(gl, evaluation.getSinks(), evaluation);

			}

			if (renderProjectionPolyhedra) {
				locationRenderer.renderLocationsProjectionPolyhedra(gl, evaluation.getSinks());
			}

			if (!renderPlan && !renderTransparent) {
				modelRenderer.renderMeshesLines(gl, m);
				modelRenderer.renderLines(gl, m);
			}

			if (renderEvaluationEdges) {
				locationRenderer.renderEvaluationEdges(gl, evaluation);
			}

			if (renderEvaluationNodeSources) {
				locationRenderer.renderEvaluationSourceLocations(gl, evaluation);
			}

			if (renderEvaluationNodeSinks) {
				locationRenderer.renderEvaluationSinkLocations(gl, evaluation);
			}

			if (renderEvaluationVisibilityLines) {
				locationRenderer.renderEvaluationSinkLocationsVisibilityLines(gl, evaluation);
			}
		}
	}

	public void renderGUI(GL2 gl, int width, int height, GLCamera cam, VisibilityInteriorsModel m) {

		if (m == null) {
			return;
		}

		if (renderEvaluation && evaluation != null) {

			if (renderEvaluationNodeSourceLabels) {
				locationRenderer.renderEvaluationSourceLocationsLabels(gl, cam, evaluation);

			}

			if (renderEvaluationNodeSinkLabels) {
				locationRenderer.renderEvaluationSinkLocationsLabels(gl, cam, evaluation);
			}
		}

		modelRenderer.renderLabels(gl, cam, m);
	}
}
