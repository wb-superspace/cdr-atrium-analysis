package rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import cdr.colour.HSVColour;
import cdr.colour.RGBColour;
import cdr.geometry.primitives.ArrayPoint3D;
import cdr.geometry.primitives.ArrayVector3D;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.joglFramework.camera.GLCamera;
import evaluations.VisibilityInteriorsEvaluation;
import math.ValueMapper;
import models.isovistProjectionModel.types.IsovistProjectionPolygon;
import models.isovistProjectionModel.types.IsovistProjectionPolyhedron;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsZone;

public class VisibilityInteriorsLocationRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	
	Float minLineWidth = 1f;
	Float maxLineWidth = 8f;
	
	Float modifiablePointScaleOuter = 10f;
	Float modifiablePointScaleInner = 6f;
	
	Float fixedPointScaleOuter = 8f;
	Float fixedPointScaleInner = 5f;
		
	public void renderEvaluationSinkLocations(GL2 gl, VisibilityInteriorsEvaluation evaluation) {		
		renderSinkLocationsFlow(gl, evaluation.getSinkValues(), evaluation.getNodeBounds());	
	}
	
	public void renderEvaluationSinkLocationsZonesFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		renderLocationsZonesFlow(gl, evaluation.getSinkValues(), evaluation.getNodeBounds());
	}
	
	public void renderEvaluationSinkLocationsZonesLines(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		renderLocationsZonesLines(gl, evaluation.getSinks());
	}
	
	public void renderEvaluationSinkLocationsVisibilityLines(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		gl.glColor3f(0f, 0f, 0f);
		gl.glLineWidth(0.1f);
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {
			for (VisibilityInteriorsLocation source : evaluation.getSources(sink)) {
				if (sink.getVisibilityPath(source).getLocations().size() == 2) {
					geometryRenderer.renderLineSegment3D(gl, new LineSegment3D(source, sink));
				}
			}
		}
	}
	
	public void renderEvaluationSinkLocationsLabels(GL2 gl, GLCamera camera, VisibilityInteriorsEvaluation evaluation) {
		
		Map<VisibilityInteriorsLocation, String> labels = new HashMap<>();
		
		for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {	
							
			String label = null;
			
			if (evaluation.isCumulator()) {
				
				label = Integer.toString(evaluation.getSinkValue(sink).intValue());
				
			} else {
				
				float val = evaluation.getSinkValue(sink);
				
				if (evaluation.getNodeBounds()[0] >= 0 && evaluation.getNodeBounds()[1] <= 1) {
					val *= 100;
				}
				
				label = String.format("%.2f", val);
			}
									
			labels.put(sink, label);
		}
		
		renderLocationsLabels(gl, camera, labels);
	}
		
	public void renderEvaluationSourceLocations(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		Map<VisibilityInteriorsLocation, Float> values = new HashMap<>();
		
		for (VisibilityInteriorsLocation source : evaluation.getSources()) {
			values.put(source, evaluation.getSourceValue(source));
		}
				
		renderSourceLocationsFlow(gl, values, evaluation.getNodeBounds());
		
	}
	
	public void renderEvaluationSourceLocationsZonesFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		Map<VisibilityInteriorsLocation, Float> values = new HashMap<>();
		
		for (VisibilityInteriorsLocation source : evaluation.getSources()) {
			values.put(source, evaluation.getSourceValue(source));
		}
						
		renderLocationsZonesFlow(gl, values, evaluation.getNodeBounds());
	}
	
	public void renderEvaluationSourceLocationsZonesLines(GL2 gl, VisibilityInteriorsEvaluation evaluation) {					
		renderLocationsZonesLines(gl, evaluation.getSources());
	}
	
	public void renderEvaluationSourceLocationsLabels(GL2 gl, GLCamera camera, VisibilityInteriorsEvaluation evaluation) {
		
		Map<VisibilityInteriorsLocation, String> labels = new HashMap<>();
		
		for (VisibilityInteriorsLocation source : evaluation.getSources()) {
			
			String label = null;
			
			if (evaluation.isCumulator()) {
				
				label = Integer.toString(evaluation.getSourceValue(source).intValue());
				
			} else {
				
				float val = evaluation.getSourceValue(source);
				
				if (evaluation.getNodeBounds()[0] >= 0 && evaluation.getNodeBounds()[1] <= 1) {
					val *= 100;
				}
				
				label = String.format("%.2f", val);
			}			
			
			labels.put(source, label);
		}
		
		renderLocationsLabels(gl, camera, labels);
	}
				
	public void renderEvaluationEdges(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		float maxCount = 1f;
				
		for (VisibilityInteriorsConnection edge : evaluation.getEdges()) {
			if (evaluation.getEdgeCount(edge) > maxCount) maxCount = evaluation.getEdgeCount(edge);
		}
						
		for (VisibilityInteriorsConnection edge : evaluation.getEdges()) {
			
			float lineWidth =( evaluation.getEdgeCount(edge) / maxCount * (maxLineWidth - minLineWidth) ) + minLineWidth;
										
			gl.glLineWidth(lineWidth);		
			gl.glColor3f(0, 0, 0);
									
			geometryRenderer.renderLineSegment3D(gl, edge.getGeometry());	
		}						
	}
		
	public void renderEvaluationZones(GL2 gl, VisibilityInteriorsEvaluation evaluation, VisibilityInteriorsModel m) {
		
		for (VisibilityInteriorsZone zone : m.getZones()) {
			
			float zoneValue = 0f;
			float zoneCount = 0f;
			
			for (VisibilityInteriorsLocation location : zone.getLocations()) {
				
				Float locationValueSource = evaluation.getSourceValue(location);
				Float locationValueSink = evaluation.getSinkValue(location);
				
				if (locationValueSink != null) {
					zoneValue += locationValueSink;
					zoneCount ++;
				
				} else if (locationValueSource != null) {
					zoneValue += locationValueSource;
					zoneCount ++;
				}
			}
			
			if (zoneCount != 0) {
				
				zoneValue /= zoneCount;
				
				float colorValue = ValueMapper.map(zoneValue, evaluation.getNodeBounds()[0], evaluation.getNodeBounds()[1], 0, 1);
				
				HSVColour c = new HSVColour() ;
				
				c.setHSV((1-colorValue) * 0.6f, 1f, 1f) ;				
				gl.glColor3f(c.red(), c.green(), c.blue());
				
				geometryRenderer.renderPolygon3DFill(gl, zone.getGeometry());
			
			} else {
				
				gl.glColor3f(0, 0, 0);
				
				// no render;
			}
			
			gl.glColor3f(0f, 0f, 0f);
			gl.glLineWidth(2f);
			
			geometryRenderer.renderPolygon3DLines(gl, zone.getGeometry());		
		}
	}
						
	public void renderSinkLocationsFlow(GL2 gl, Map<VisibilityInteriorsLocation, Float> sinks, float[] bounds) {
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> sinkEntry : sinks.entrySet()) {
			
			float value = ValueMapper.map(sinkEntry.getValue(), bounds[0], bounds[1], 0, 1);
			
			HSVColour c = new HSVColour() ;	
			c.setHSV((1- value) * 0.6f, 1f, 1f) ;	
					
			if (sinkEntry.getKey().isModifiable()) {
				
				gl.glPointSize(10);
				gl.glColor3f(c.red(), c.green(), c.blue());
							
				geometryRenderer.renderPoint3D(gl, sinkEntry.getKey());	
				
				gl.glPointSize(6);
				gl.glColor3f(0f,0f,0f);
				
				geometryRenderer.renderPoint3D(gl, sinkEntry.getKey());	
				
			} else {
			
				gl.glPointSize(8);
				gl.glColor3f(c.red(), c.green(), c.blue());
				
				geometryRenderer.renderPoint3D(gl, sinkEntry.getKey());	
				
				gl.glPointSize(5);
				gl.glColor3f(0f,0f,0f);
										
				geometryRenderer.renderPoint3D(gl, sinkEntry.getKey());	
			}
		}	
	}
	
	public void renderSourceLocationsFlow(GL2 gl, Map<VisibilityInteriorsLocation, Float> sources, float[] bounds) {
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> sourceEntry : sources.entrySet()) {
			
			float value = ValueMapper.map(sourceEntry.getValue(), bounds[0], bounds[1], 0, 1);
						
			HSVColour c = new HSVColour() ;					
			c.setHSV((1- value) * 0.6f, 1f, 1f) ;	
			
			if (sourceEntry.getKey().isModifiable()) {
				
				gl.glPointSize(10);
				gl.glColor3f(0f,0f,0f);
							
				geometryRenderer.renderPoint3D(gl, sourceEntry.getKey());	
				
				gl.glPointSize(6);
				gl.glColor3f(c.red(), c.green(), c.blue());
				
				
				geometryRenderer.renderPoint3D(gl, sourceEntry.getKey());	
				
			} else {
				
				gl.glPointSize(8);
				gl.glColor3f(0f,0f,0f);
							
				geometryRenderer.renderPoint3D(gl, sourceEntry.getKey());	
				
				gl.glPointSize(5);
				gl.glColor3f(c.red(), c.green(), c.blue());
				
				
				geometryRenderer.renderPoint3D(gl, sourceEntry.getKey());	
			}
		}	
	}
	
	public void renderLocationsZonesFlow(GL2 gl, Map<VisibilityInteriorsLocation, Float> locations, float[] bounds) {
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> locationEntry : locations.entrySet()) {
			
			float value = ValueMapper.map(locationEntry.getValue(), bounds[0], bounds[1], 0, 1);
						
			HSVColour c = new HSVColour() ;					
			c.setHSV((1- value) * 0.6f, 1f, 1f) ;	
						
			if (locationEntry.getKey().getZone() != null) {
				
				gl.glColor3f(c.red(), c.green(), c.blue());
				
				geometryRenderer.renderPolygon3DFill(gl, locationEntry.getKey().getZone().getGeometry());
				
			}
		}	
	}
	
	public void renderLocationsZonesLines(GL2 gl, Collection<VisibilityInteriorsLocation> locations) {
		
		for (VisibilityInteriorsLocation location : locations) {
									
			if (location.getZone() != null) {
				
				gl.glColor3f(0f, 0f, 0f);
				gl.glLineWidth(1f);
				
				geometryRenderer.renderPolygon3DLines(gl, location.getZone().getGeometry());
				
			}
		}	
	}
	
	public void renderLocationsVisibilityLines(GL2 gl, Collection<VisibilityInteriorsLocation> locations) {
		
		for (VisibilityInteriorsLocation location : locations) {
			renderLocationVisibilityLines(gl, location);
		}
	}
	
	public void renderLocationVisibilityLines(GL2 gl, VisibilityInteriorsLocation location) {
		
		gl.glColor3f(0f, 0f, 0f);
		gl.glLineWidth(0.1f);
		
		for (VisibilityInteriorsLocation sink : location.getVisibilityPathLocations()) {
			if (location.getVisibilityPath(sink).getLocations().size() == 2) {
				geometryRenderer.renderLineSegment3D(gl, new LineSegment3D(location, sink));
			}
		}
	}
	
	public void renderLocationsProjectionPolyhedra(GL2 gl, Collection<VisibilityInteriorsLocation> collection) {
		
		float alpha = 0.2f;
		
		for (VisibilityInteriorsLocation location : collection) {
			alpha *= 0.99f;
		}
		
		for (VisibilityInteriorsLocation location : collection) {
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
			
//		gl.glEnable(GL.GL_BLEND);
//		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glLineWidth(0.3f);
		gl.glColor4f(0f, 0f, 0f, alpha);
		
		geometryRenderer.renderPolygonsWithHoles3DFill(gl, render);
		geometryRenderer.renderPolygonsWithHoles3DLines(gl, render);
		
		//gl.glDisable(GL.GL_BLEND);
	}
	
	public void renderLocationsProjectionPolygons(GL2 gl, Collection<VisibilityInteriorsLocation> collection, VisibilityInteriorsEvaluation evaluation) {
		
		float alpha = 1 / evaluation.getProjectionOverlapBounds()[1];
		
		for (VisibilityInteriorsLocation location : collection) {
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
		
//		gl.glEnable(GL.GL_BLEND);
//		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4f(1f, 0f, 0f, alpha);
		
		geometryRenderer.renderPolygonsWithHoles3DFill(gl, render);	
		
		gl.glLineWidth(0.1f);
		gl.glColor3f(0f, 0f, 0f);
		
		geometryRenderer.renderPolygonsWithHoles3DLines(gl, render);
		
		//gl.glDisable(GL.GL_BLEND);
	}
	
	public void renderLocationsLabels(GL2 gl, GLCamera camera, Map<VisibilityInteriorsLocation, String> locations) {
		
		for (Map.Entry<VisibilityInteriorsLocation, String> locationEntry : locations.entrySet()) {
			renderLocationLabel(gl, camera, locationEntry.getKey(), locationEntry.getValue());
		}	
	}
	
	
	public void renderLocationLabel(GL2 gl, GLCamera camera, VisibilityInteriorsLocation location, String label) {
		
		GLUT glut = new GLUT();
		
		String nLabel = label;
		
		Point3D vProj = new ArrayPoint3D();
		
		if(!camera.project(location, vProj)) {
			
			return;
		}
				
		gl.glPushMatrix() ;
		gl.glTranslatef(vProj.x() + 5, vProj.y(), vProj.z()) ;
		gl.glScalef(.075f, .075f, .075f) ;
		
		float width = glut.glutStrokeLengthf(GLUT.STROKE_ROMAN, nLabel) ;
				
//		geometryRenderer.setColour(gl, RGBColour.WHITE()) ;
//		geometryRenderer.renderRectangleFill(gl, new Rectangle2D(-10, -10, width + 20, 140)) ;
		
		geometryRenderer.setColour(gl, RGBColour.BLACK()) ;
		geometryRenderer.setLineWidth(gl, .5f) ;
		glut.glutStrokeString(GLUT.STROKE_ROMAN, nLabel) ;
				
		gl.glPopMatrix() ;
	}
}
