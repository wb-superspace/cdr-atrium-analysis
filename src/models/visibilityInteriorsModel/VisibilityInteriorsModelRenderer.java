package models.visibilityInteriorsModel;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.media.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

import cdr.colour.HSVColour;
import cdr.fileIO.dxf2.DXFDocument2;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.graph.renderer.GraphRenderer;
import cdr.joglFramework.camera.GLCamera;
import evaluations.EvaluationField;
import math.ValueMapper;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import templates.Model;
import templates.ModelRenderer;

public class VisibilityInteriorsModelRenderer implements ModelRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	GraphRenderer graphRenderer = new GraphRenderer();	
	TextRenderer textRenderer = new TextRenderer(new Font("WBHELVETICANUEUE", Font.PLAIN, 10));
	
	GLUT glut = new GLUT();
	
	private EvaluationField evaluationField;
	private String evaluationLabel;
		
	@Override
	public void renderFill(GL2 gl, Model model) {

		if (model == null) {
			return;
		}
		
		VisibilityInteriorsModel m = (VisibilityInteriorsModel) model;
		
		if (evaluationField != null) {
			this.renderEvaluationField(gl, m);
		}	

		this.renderWallsFill(gl, m);
		this.renderSolidsFill(gl, m);
		this.renderConnections(gl, m);		
	}
	
	@Override
	public void renderLines(GL2 gl, Model model) {
		
		if (model == null) {
			return;
		}
		
		VisibilityInteriorsModel m = (VisibilityInteriorsModel) model;
		
		this.renderLayoutsLines(gl, m);
	}
	
	@Override
	public void renderGUI(GL2 gl, int width, int height, GLCamera cam, Model model) {
		
		if (model == null) {
			return;
		}	
	}
		
	public void setEvaluationField(EvaluationField evaluationField) {		
		this.evaluationField = evaluationField;		
	}
	
	public void setEvaluationLabel(String evaluationLabel) {
		this.evaluationLabel = evaluationLabel;
	}
		
	private void renderEvaluationField (GL2 gl, VisibilityInteriorsModel m) {
								
		HashMap<Float, HashMap<Point3D, Float>> results = evaluationField.getValues(evaluationLabel);
		float[] domain = evaluationField.getValueDomain(evaluationLabel);
						
		if (results == null) {
			return;
		}
		
		gl.glBegin(GL2.GL_QUADS) ;
		
		Float min = null;
		Float max = null;
		
		for (Float z : results.keySet()) {
						
			for (Point3D p : results.get(z).keySet()) {
				
				float val = results.get(z).get(p);
				
				if (min == null || val < min) {
					min = val;
				}
				
				if (max == null || val > max) {
					max = val;
				}
			}
		}
				
		for (Float z : results.keySet()) {
			
			for (Point3D p : results.get(z).keySet()) {
				
				float val = results.get(z).get(p);
				float remap = ValueMapper.map(val, min, max, 0, 1);
				
				HSVColour c = new HSVColour() ;
				c.setHSV((1-(remap)) * 0.6f, 1f, 1f) ;						
				gl.glColor3f(c.red(), c.green(), c.blue()) ;
				
				float dx = evaluationField.getResoluation();
				float dy = evaluationField.getResoluation();
				
				gl.glVertex3f(p.x()-dx/2f, p.y()-dy/2f, p.z()) ;
				gl.glVertex3f(p.x()+dx/2f, p.y()-dy/2f, p.z()) ;
				gl.glVertex3f(p.x()+dx/2f, p.y()+dy/2f, p.z()) ;
				gl.glVertex3f(p.x()-dx/2f, p.y()+dy/2f, p.z()) ;
			}
		}
										
		gl.glEnd() ;
	}
							
	private void renderLayoutsLines(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glLineWidth(1f);
		gl.glColor3f(0f,0f,0f);
		
		List<Polygon3D> pgons = new ArrayList<>();
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			pgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter()));
			pgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));
			
			for (IsovistProjectionPolygon wall : layout.getGeometry(IsovistProjectionGeometryType.WALL, 
					new IsovistProjectionFilter())) {
				
				if (wall.getOuterTypes().isEmpty()) {
					continue;
				}
				
				pgons.add(wall);
			}
		}
		
		geometryRenderer.renderPolygons3DLines(gl, pgons);
	}
		
	private void renderWallsFill(GL2 gl, VisibilityInteriorsModel m) {
		
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
	
	private void renderSolidsFill(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glColor3f(0.4f,0.4f,0.4f) ;
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {

			for (IsovistProjectionPolygon solid : layout.getGeometry(IsovistProjectionGeometryType.SOLID, 
					new IsovistProjectionFilter())) {
				
				solid = new IsovistProjectionPolygon(solid.iterablePoints());
				geometryRenderer.renderPolygon3DFill(gl, solid);
				solid.reverseWinding();
				geometryRenderer.renderPolygon3DFill(gl, solid);
			}
		}
	}
		
	private void renderConnections(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glColor3f(0,0,0) ;
		gl.glLineWidth(3f);
		
		geometryRenderer.renderLineSegments3D(gl, m.getConnections());
	}
}
