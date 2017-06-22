package rendering;

import java.awt.datatransfer.FlavorTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;
import com.sun.org.apache.bcel.internal.generic.FLOAD;

import cdr.colour.HSVColour;
import cdr.colour.RGBColour;
import cdr.geometry.primitives.ArrayPoint3D;
import cdr.geometry.primitives.ArrayVector3D;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.geometry.primitives.Rectangle2D;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.joglFramework.camera.GLCamera;
import evaluations.VisibilityInteriorsEvaluation;
import math.ValueMapper;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.isovistProjectionModel3d.IsovistProjectionPolyhedron;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;
import models.visibilityInteriorsModel.types.VisibilityInteriorsZone;

public class VisibilityInteriorsLocationRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	
	Float minLineWidth = 0.5f;
	Float maxLineWidth = 5f;
	
	public void renderLocationsEvaluationGraph(GL2 gl, List<VisibilityInteriorsLocation> locations, int index) {
		
		String label = null;
		
		List<VisibilityInteriorsEvaluation> evaluations = new ArrayList<>();
		
		for (VisibilityInteriorsLocation location : locations) {
			if (location.getEvaluation(index) != null) {
				evaluations.add(location.getEvaluation(index));
				label = location.getEvaluation(index).getLabel();
			}
		}
		
		if (label != null) {
			renderEvaluationGraph(gl, VisibilityInteriorsEvaluation.mergeEvaluations(label, evaluations));
		}		
	}
			
	public void renderLocationEvaluationGraph(GL2 gl, VisibilityInteriorsLocation location, int index) {
		
		if (location.getEvaluation(index) != null) {
			renderEvaluationGraph(gl, location.getEvaluation(index));
		}
	}
	
	public void renderEvaluationGraph(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		float maxCount = 1f;
		
		float minValue = Float.MAX_VALUE;
		float maxValue = -Float.MAX_VALUE;
		
		for (LineSegment3D edge : evaluation.getEdges()) {
			if (evaluation.getEdgeCount(edge) > maxCount) maxCount = evaluation.getEdgeCount(edge);
			if (evaluation.getEdgeValue(edge) > maxValue) maxValue = evaluation.getEdgeValue(edge);
			if (evaluation.getEdgeValue(edge) < minValue) minValue = evaluation.getEdgeValue(edge);
		}
						
		for (LineSegment3D edge : evaluation.getEdges()) {
			
			float lineWidth =( evaluation.getEdgeCount(edge) / maxCount * (maxLineWidth - minLineWidth) ) + minLineWidth;
			float lineValue = ValueMapper.map(evaluation.getEdgeValue(edge), minValue, maxValue, 0, 1);
			
			HSVColour c = new HSVColour() ;
				
			c.setHSV((1-lineValue) * 0.6f, 1f, 1f) ;	
			
			gl.glLineWidth(lineWidth);
			gl.glPointSize(5);
			gl.glColor3f(c.red(), c.green(), c.blue());
						
			geometryRenderer.renderLineSegment3D(gl, edge);	
		}
		
		gl.glColor3f(0.5f, 0.5f, 0.5f);
		gl.glLineWidth(0.2f);
								
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {		
			renderSourceLocations(gl, evaluation.getSources(sink));
			
		}
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {			
			renderSinkLocations(gl, Arrays.asList(sink));
		}
	}
	
	public void renderLocationsEvaluationNodes(GL2 gl, List<VisibilityInteriorsLocation> locations, int index) {
		
		String label = null;
		
		List<VisibilityInteriorsEvaluation> evaluations = new ArrayList<>();
		
		for (VisibilityInteriorsLocation location : locations) {
			if (location.getEvaluation(index) != null) {
				evaluations.add(location.getEvaluation(index));
				label = location.getEvaluation(index).getLabel();
			}
		}
		
		if (label != null) {
			renderEvaluationNodes(gl, VisibilityInteriorsEvaluation.mergeEvaluations(label, evaluations));
		}		
	}
	
	public void renderLocationEvaluationNodes(GL2 gl, VisibilityInteriorsLocation location, int index) {
		
		if (location.getEvaluation(index) != null) {
			renderEvaluationNodes(gl, location.getEvaluation(index));
		}	
	}
	
	public void renderEvaluationNodes(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		Map<VisibilityInteriorsLocation, Float> sourceValues = new HashMap<>();
		
		for (LineSegment3D edge : evaluation.getEdges()) {
			
			gl.glColor3f(0f, 0f, 0f);
			gl.glLineWidth(0.2f);
			
			geometryRenderer.renderLineSegment3D(gl, edge);	
		}
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {					
			sourceValues.putAll(evaluation.getSourceValues(sink));
			
		}
	
		renderSourceLocationsValues(gl, sourceValues);
		renderSinkLocationsValues(gl, evaluation.getSinkValues());
	}
	
	public void renderLocationsEvaluationVisibilityGraphPaths(GL2 gl, List<VisibilityInteriorsLocation> locations, int index) {
		
		for (VisibilityInteriorsLocation location : locations) {
			renderLocationEvaluationVisibilityGraphPaths(gl, location, index);
		}
	}
	
	public void renderLocationEvaluationVisibilityGraphPaths(GL2 gl, VisibilityInteriorsLocation location, int index) {
	
		if (location.getEvaluation(index) != null) {
			renderEvaluationVisibilityGraphPaths(gl, location.getEvaluation(index));
		}	
	}
	
	public void renderEvaluationVisibilityGraphPaths(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		gl.glColor3f(0.5f, 0.5f, 0.5f);
		gl.glLineWidth(0.2f);
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {					
			for (VisibilityInteriorsLocation source : evaluation.getSources(sink)) {
				if (sink.getVisibilityPath(source).getLocations().size() == 2) {
					geometryRenderer.renderLineSegments3D(gl, sink.getVisibilityPath(source).getGeometry());
				}
			}
		}
		
	}
	
	public void renderLocationEvaluationLabels(GL2 gl, GLCamera camera,  VisibilityInteriorsLocation location, int index) {
		
		VisibilityInteriorsEvaluation evaluation = location.getEvaluation(index);
		
		if (evaluation == null) {
			return;
		}
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {		
			renderLocationLabel(gl, camera, sink, Float.toString(evaluation.getSinkValue(sink)));
		}
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {			
			
			for (VisibilityInteriorsLocation source : evaluation.getSources(sink)) {
				renderLocationLabel(gl, camera, source, Float.toString(evaluation.getSourceValue(sink, source)));
			}
		}
	}
	
	public void renderZone(GL2 gl, VisibilityInteriorsZone zone) {
		
		gl.glColor3f(1f, 1f, 1f);
		geometryRenderer.renderPolygon3DFill(gl, zone.getGeometry());
		
		gl.glColor3f(0f, 0f, 0f);
		gl.glLineWidth(2f);
		
		geometryRenderer.renderPolygon3DLines(gl, zone.getGeometry());
	}
	
	public void renderSourceLocations(GL2 gl, Collection<VisibilityInteriorsLocation> collection) {
		
		for (VisibilityInteriorsLocation source : collection) {
			
			gl.glPointSize(8);
			gl.glColor3f(1f,1f,1f);
			
			geometryRenderer.renderPoint3D(gl, source);
			
			gl.glPointSize(5);
			gl.glColor3f(0f,0f,0f);
			
			geometryRenderer.renderPoint3D(gl, source);
		}
		
	}
	
	public void renderSinkLocations(GL2 gl, Collection<VisibilityInteriorsLocation> sinks) {
		
		for (VisibilityInteriorsLocation sink : sinks) {
			
			gl.glPointSize(10);
			gl.glColor3f(0f,0f,0f);
			
			geometryRenderer.renderPoint3D(gl, sink);
			
			gl.glPointSize(6);
			gl.glColor3f(1f,1f,1f);
			
			geometryRenderer.renderPoint3D(gl, sink);
		}
	}
	
	public void renderSinkLocationsValues(GL2 gl, Map<VisibilityInteriorsLocation, Float> sinks) {
		
		Float minValue = Float.MAX_VALUE;
		Float maxValue = -Float.MAX_VALUE;
		
		for (Float value : sinks.values()) {
			
			if (value > maxValue) maxValue = value;
			if (value < minValue) minValue = value;
		}
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> sinkEntry : sinks.entrySet()) {
			
			float value = ValueMapper.map(sinkEntry.getValue(), minValue, maxValue, 0, 1);
					
			HSVColour c = new HSVColour() ;
						
			c.setHSV((1- value) * 0.6f, 1f, 1f) ;	
			
			gl.glPointSize(10);
			gl.glColor3f(c.red(), c.green(), c.blue());
						
			geometryRenderer.renderPoint3D(gl, sinkEntry.getKey());	
			
			gl.glPointSize(6);
			gl.glColor3f(0f,0f,0f);
			
			geometryRenderer.renderPoint3D(gl, sinkEntry.getKey());	
		}	
	}
	
	public void renderSourceLocationsValues(GL2 gl, Map<VisibilityInteriorsLocation, Float> sources) {
		
		Float minValue = Float.MAX_VALUE;
		Float maxValue = -Float.MAX_VALUE;
		
		for (Float value : sources.values()) {
			
			if (value > maxValue) maxValue = value;
			if (value < minValue) minValue = value;
		}
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> sourceEntry : sources.entrySet()) {
			
			float value = ValueMapper.map(sourceEntry.getValue(), minValue, maxValue, 0, 1);
					
			HSVColour c = new HSVColour() ;
						
			c.setHSV((1- value) * 0.6f, 1f, 1f) ;	
			
			gl.glPointSize(8);
			gl.glColor3f(1f,1f,1f);
						
			geometryRenderer.renderPoint3D(gl, sourceEntry.getKey());	
			
			gl.glPointSize(5);
			gl.glColor3f(c.red(), c.green(), c.blue());
			
			
			geometryRenderer.renderPoint3D(gl, sourceEntry.getKey());	
		}	
	}
	
	public void renderLocationsVisibilityLines(GL2 gl, List<VisibilityInteriorsLocation> locations) {
		
		for (VisibilityInteriorsLocation location : locations) {
			renderLocationVisibilityLines(gl, location);
		}
	}
	
	public void renderLocationVisibilityLines(GL2 gl, VisibilityInteriorsLocation location) {
		
		gl.glColor3f(0.5f, 0.5f, 0.5f);
		gl.glLineWidth(0.2f);
		
		for (VisibilityInteriorsLocation sink : location.getVisibilityPathLocations()) {
			if (location.getVisibilityPath(sink).getLocations().size() == 2) {
				geometryRenderer.renderLineSegment3D(gl, new LineSegment3D(location, sink));
			}
		}
	}
	
	public void renderLocationsProjectionPolyhedra(GL2 gl, List<VisibilityInteriorsLocation> locations) {
		
		float alpha = 0.2f;
		
		for (VisibilityInteriorsLocation location : locations) {
			alpha *= 0.99f;
		}
		
		for (VisibilityInteriorsLocation location : locations) {
			renderLocationProjectionPolyhedra(gl, location, alpha);
		}
	}
		
	public void renderLocationProjectionPolyhedra(GL2 gl, VisibilityInteriorsLocation location) {
		renderLocationProjectionPolyhedra(gl, location, 0.2f);
	}
	
	private void renderLocationProjectionPolyhedra(GL2 gl, VisibilityInteriorsLocation location, float alpha) {
		
		List<Polygon3DWithHoles> render = new ArrayList<>();
		
		for (List<IsovistProjectionPolyhedron> polyhedrons : location.getProjectionPolyhedra().values()) {
			
			for (IsovistProjectionPolyhedron polyhedron : polyhedrons) {
				for (IsovistProjectionPolygon side : polyhedron.getSides()) {
					render.add(side.getPolygon3DWithHoles());
				}
			}
		}
			
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(0f, 0f, 0f, alpha);
		
		geometryRenderer.renderPolygonsWithHoles3DFill(gl, render);
		geometryRenderer.renderPolygonsWithHoles3DLines(gl, render);
		
		gl.glDisable(GL.GL_BLEND);
	}
	
	public void renderLocationsProjectionPolygons(GL2 gl, List<VisibilityInteriorsLocation> locations) {
		
		float alpha = 0.2f;
		
		for (VisibilityInteriorsLocation location : locations) {
			alpha *= 0.99f;
		}
		
		for (VisibilityInteriorsLocation location : locations) {
			renderLocationProjectionPolygons(gl, location, alpha);
		}
	}
	
	public void renderLocationProjectionPolygons(GL2 gl, VisibilityInteriorsLocation location) {
		renderLocationProjectionPolygons(gl, location, 0.2f);
	}
	
	private void renderLocationProjectionPolygons(GL2 gl, VisibilityInteriorsLocation location, float alpha) {
		
		List<Polygon3DWithHoles> render = new ArrayList<>();
		
		for (List<IsovistProjectionPolygon> projections : location.getProjectionPolygons().values()) {
			
			for (IsovistProjectionPolygon projection: projections) {
				
				Polygon3DWithHoles renderPolygon = projection.getPolygon3DWithHoles();
				
				renderPolygon.translate(new ArrayVector3D(0, 0, 0.01f));
				
				render.add(renderPolygon);
			}
		}
		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(0f, 0f, 0f, alpha);
		
		geometryRenderer.renderPolygonsWithHoles3DFill(gl, render);	
		
		gl.glLineWidth(0.1f);
		gl.glColor3f(0f, 0f, 0f);
		
		geometryRenderer.renderPolygonsWithHoles3DLines(gl, render);
		
		gl.glDisable(GL.GL_BLEND);
	}
	
	public void renderLocationsLabels(GL2 gl, GLCamera camera, Map<VisibilityInteriorsLocation, String> locations) {
		
		for (Map.Entry<VisibilityInteriorsLocation, String> locationEntry : locations.entrySet()) {
			renderLocationLabel(gl, camera, locationEntry.getKey(), locationEntry.getValue());
		}	
	}
	
	public void renderLocationLabel(GL2 gl, GLCamera camera, VisibilityInteriorsLocation location, String label) {
		
		GLUT glut = new GLUT();
		
		Point3D vProj = new ArrayPoint3D();
		
		if(!camera.project(location, vProj)) {
			
			return;
		}
				
		gl.glPushMatrix() ;
		gl.glTranslatef(vProj.x() + 5, vProj.y(), vProj.z()) ;
		gl.glScalef(.075f, .075f, .075f) ;
		
		float width = glut.glutStrokeLengthf(GLUT.STROKE_ROMAN, label) ;
		
		geometryRenderer.setColour(gl, RGBColour.WHITE()) ;
		geometryRenderer.renderRectangleFill(gl, new Rectangle2D(-10, -10, width + 20, 140)) ;
		
		geometryRenderer.setColour(gl, RGBColour.BLACK()) ;
		geometryRenderer.setLineWidth(gl, .5f) ;
		glut.glutStrokeString(GLUT.STROKE_ROMAN, label) ;
				
		gl.glPopMatrix() ;
	}
}
