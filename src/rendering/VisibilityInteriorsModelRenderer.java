package rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

import cdr.colour.RGBColour;
import cdr.geometry.primitives.ArrayPoint3D;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Rectangle2D;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.joglFramework.camera.GLCamera;
import cdr.mesh.renderer.MeshRenderer3DOutline;
import evaluations.VisibilityInteriorsEvaluation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.VisibilityInteriorsModel.VisibilityInteriorsModel;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsLayout;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.VisibilityInteriorsModel.types.VisibilityInteriorsZone;



public class VisibilityInteriorsModelRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	MeshRenderer3DOutline meshRenderer = new MeshRenderer3DOutline();
		
	VisibilityInteriorsLocationRenderer locationRenderer = new VisibilityInteriorsLocationRenderer();
	
	VisibilityInteriorsEvaluation evaluation = null;
	
	public boolean renderVisibilityGraph = false;
	public boolean renderConnectivityGraph = false;
	
	public boolean renderProjectionPolygons = false;
	public boolean renderProjectionPolyhedra = false;
	
	public boolean renderEvaluation = true;
	public boolean renderEvaluationLabels = false;
	public boolean renderEvaluationNodes = true;
	
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
	
	public void update(VisibilityInteriorsEvaluation evaluation) {
		this.evaluation = evaluation;
	}
			
	public void renderFill(GL2 gl, VisibilityInteriorsModel m) {

		if (m == null) {
			return;
		}
		
		if (!renderPlan) {
			
			if (renderMesh && !renderTransparent) {			
				this.renderModelLayoutMeshesFill(gl, m);
			}
		
		} else {
			
			this.renderModelLayoutsWallsFill(gl, m);
			this.renderModelLayoutsFloorsFill(gl, m);
		}
		
		gl.glColor3f(0.2f, 0.2f, 0.2f);
		geometryRenderer.renderPolygons3DFill(gl, m.ref);
		gl.glLineWidth(1);
		gl.glColor3f(0f, 0f, 0f);
		geometryRenderer.renderPolygons3DLines(gl, m.ref);

	}
	
	public void renderLines(GL2 gl, VisibilityInteriorsModel m) {
		
		if (m == null) {
			return;
		}
		
		if (!renderPlan) {
			
			if (renderMesh && renderTransparent) {
				this.renderModelLayoutMeshesFill(gl, m);
				this.renderModelInteriorAtrium(gl, m);
			} 
			
			if (renderTransparent) {	
				this.renderModelLayoutsLinesThin(gl, m);
				
			} else {
				this.renderModelLayoutMeshesLines(gl, m);
				this.renderModelLayoutsLines(gl, m);
			}
			
		} else {
			this.renderModelLayoutsLines(gl, m);
		}		

		this.renderModelConnections(gl, m);
							
		if (renderProjectionPolyhedra) {
			locationRenderer.renderLocationsProjectionPolyhedra(gl, evaluation.getSinks());
		}
		
		if (renderProjectionPolygons) {
			locationRenderer.renderLocationsProjectionPolygons(gl, evaluation.getSinks());
		}
		
		if (renderConnectivityGraph) {
			this.renderModelConnectivityGraph(gl, m);
		}
		
		if (renderVisibilityGraph) {
			this.renderModelVisibilityGraph(gl, m);
		}
						
		if (renderEvaluation && evaluation != null) {
			
			if (renderEvaluationZones) {
				locationRenderer.renderEvaluationZones(gl, evaluation, m);
			}
			
			if (renderEvaluationEdges) {
				locationRenderer.renderEvaluationEdges(gl, evaluation);
			}
			
			if (renderEvaluationNodes) {
				
				if (renderEvaluationNodeSources) {
					locationRenderer.renderEvaluationSourceLocations(gl, evaluation);
				}
						
				if (renderEvaluationNodeSinks) {
					locationRenderer.renderEvaluationSinkLocations(gl, evaluation);
				}
				
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
		
		renderModelLayoutLabels(gl, cam, m);
				
	}
		
	private void renderModelConnectivityGraph(GL2 gl, VisibilityInteriorsModel m) {
		
		if (m.getConnectivityGraph() == null) return;
		
		Graph3D connectivityGraph = m.getConnectivityGraph();
		
		gl.glColor3f(0f, 0f, 0f);
		gl.glLineWidth(1f);
		
		for (GraphEdge edge : connectivityGraph.iterableEdges()) {
			geometryRenderer.renderLineSegment3D(gl, connectivityGraph.getEdgeData(edge));
		}
		
		locationRenderer.renderSinkLocations(gl, m.getLocationsModifiable());
		
	}
	
	private void renderModelVisibilityGraph(GL2 gl, VisibilityInteriorsModel m) {
		
		if (m.getVisibilityGraph() == null) return;
		
		Graph3D visibilityGraph = m.getVisibilityGraph();
		
		gl.glLineWidth(0.1f);
		gl.glColor3f(0.5f,0.6f,0.5f);
		
		for (GraphEdge edge : visibilityGraph.iterableEdges()) {
			geometryRenderer.renderLineSegment3D(gl, visibilityGraph.getEdgeData(edge));
		}
		
	}
			
	private void renderModelConnections(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glLineWidth(3);
		gl.glColor3f(0f,0f,0f);
		
		for (VisibilityInteriorsConnection connection : m.getConnections()) {
			geometryRenderer.renderLineSegment3D(gl, connection.getGeometry());
		}
	}
	
	private void renderModelConnectionsEvaluation(GL2 gl, VisibilityInteriorsModel m, VisibilityInteriorsEvaluation e) {
		
		gl.glLineWidth(3);
		gl.glColor3f(0f,0f,0f);
		
		for (VisibilityInteriorsConnection connection : m.getConnections()) {
			if (!e.getEdges().contains(connection)) {
				geometryRenderer.renderLineSegment3D(gl, connection.getGeometry());
			}	
		}
	}
	
	private void renderModelLayoutLabels(GL2 gl, GLCamera camera, VisibilityInteriorsModel m) {
		
		int count = 1;
		float leftOffset = 30f;
		
		for (Entry<Float, VisibilityInteriorsLayout> layoutEntry : m.getLayouts().entrySet()) {
			
			GLUT glut = new GLUT();
			
			Point3D vProj = new ArrayPoint3D();
			
			Point3D location = layoutEntry.getValue().getAnchor();
			String label = "FLOOR " + count;
			
			if(!camera.project(location, vProj)) {
				
				return;
			}
					
			gl.glPushMatrix() ;
			gl.glTranslatef(leftOffset, vProj.y(), vProj.z()) ;
			gl.glScalef(.075f, .075f, .075f) ;
			
			float width = glut.glutStrokeLengthf(GLUT.STROKE_ROMAN, label) ;
			
			geometryRenderer.setColour(gl, RGBColour.WHITE()) ;
			geometryRenderer.renderRectangleFill(gl, new Rectangle2D(-10, -10, width + 20, 140)) ;
			
			geometryRenderer.setColour(gl, RGBColour.BLACK()) ;
			geometryRenderer.setLineWidth(gl, .5f) ;
					
			glut.glutStrokeString(GLUT.STROKE_ROMAN, label) ;
					
			gl.glPopMatrix() ;
			
			geometryRenderer.setLineWidth(gl, .1f) ;
			geometryRenderer.renderLineSegment3D(gl, new LineSegment3D(new ArrayPoint3D(leftOffset, vProj.y(), vProj.z()), vProj));
			
			count++;
		}	
	}
							
	private void renderModelLayoutsLines(GL2 gl, VisibilityInteriorsModel m) {
				
		List<Polygon3D> innerPgons = new ArrayList<>();
		List<Polygon3D> outerPgons = new ArrayList<>();
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			outerPgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter()));
			innerPgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));
			
			for (IsovistProjectionPolygon wall : layout.getGeometry(IsovistProjectionGeometryType.WALL, 
					new IsovistProjectionFilter())) {
				
				if (wall.getOuterTypes().isEmpty()) {
					continue;
				}
				
				outerPgons.add(wall);
			}
		}
		
		gl.glLineWidth(1f);
		gl.glColor3f(0f,0f,0f);
		
		geometryRenderer.renderPolygons3DLines(gl, outerPgons);
		
		gl.glLineWidth(3f);
		gl.glColor3f(0f,0f,0f);
		
		geometryRenderer.renderPolygons3DLines(gl, innerPgons);
	}
	
	private void renderModelLayoutsLinesThin(GL2 gl, VisibilityInteriorsModel m) {
		
		List<Polygon3D> innerPgons = new ArrayList<>();
		List<Polygon3D> outerPgons = new ArrayList<>();
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			outerPgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter()));
			innerPgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));
			
			for (IsovistProjectionPolygon wall : layout.getGeometry(IsovistProjectionGeometryType.WALL, 
					new IsovistProjectionFilter())) {
				
				if (wall.getOuterTypes().isEmpty()) {
					continue;
				}
				
				innerPgons.add(wall);
			}
		}
				
		gl.glLineWidth(0.5f);
		gl.glColor3f(0f,0f,0f);
		
		geometryRenderer.renderPolygons3DLines(gl, outerPgons);
		
		gl.glLineWidth(1f);
		gl.glColor3f(0f,0f,0f);
		
		geometryRenderer.renderPolygons3DLines(gl, innerPgons);
	}
	
	private void renderModelInteriorAtrium(GL2 gl, VisibilityInteriorsModel m ) {
		
		List<Polygon3D> innerPgons = new ArrayList<>();
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {		
			innerPgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));
		}
		
//		gl.glEnable(GL.GL_BLEND);
//		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(0f, 0f, 0f, 0.2f);
		
		geometryRenderer.renderPolygons3DFill(gl, innerPgons);
		
//		gl.glDisable(GL.GL_BLEND);
	}
	
	private void renderModelLayoutMeshesFill(GL2 gl, VisibilityInteriorsModel m) {
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			gl.glColor3f(0.5f,0.5f,0.5f);
			
			meshRenderer.renderFill(gl, layout.getRenderMeshesFloor());
			
			gl.glColor3f(0.4f,0.4f,0.4f);
			
			meshRenderer.renderFill(gl, layout.getRenderMeshesWall());
		}
	}
	
	private void renderModelLayoutMeshesLines(GL2 gl, VisibilityInteriorsModel m) {
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
						
			gl.glLineWidth(0.01f);
			gl.glColor3f(0, 0, 0);
										
			meshRenderer.renderCornerEdges(gl, layout.getRenderMeshesFloor(), 0.1f);	
			meshRenderer.renderBoundaryEdges(gl, layout.getRenderMeshesFloor());
			
			meshRenderer.renderCornerEdges(gl, layout.getRenderMeshesWall(), 0.1f);	
			meshRenderer.renderBoundaryEdges(gl, layout.getRenderMeshesWall());
		}
	}
		
	private void renderModelLayoutsWallsFill(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glColor3f(0.4f,0.4f,0.4f) ;
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {

			for (IsovistProjectionPolygon wall : layout.getGeometry(IsovistProjectionGeometryType.WALL, 
					new IsovistProjectionFilter())) {
				
				if (wall.getOuterTypes().isEmpty()) {
					continue;
				}
				
				wall = new IsovistProjectionPolygon(wall.iterablePoints());
				geometryRenderer.renderPolygon3DFill(gl, wall);
				wall.reverseWinding();
				geometryRenderer.renderPolygon3DFill(gl, wall);
			}
		}
	}
	
	private void renderModelLayoutsFloorsFill(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glColor3f(0.5f,0.5f,0.5f);
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
				geometryRenderer.renderPolygonsWithHoles3DFill(gl, layout.buildPolygonsWithHoles(true, false, false, 0, 0));
		}
	}
}
