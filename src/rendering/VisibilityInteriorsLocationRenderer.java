package rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import org.jcolorbrewer.ColorBrewer;

import com.jogamp.opengl.util.gl2.GLUT;

import cdr.colour.Colour;
import cdr.colour.HSVColour;
import cdr.colour.RGBColour;
import cdr.colour.scale.LinearInterpolationColorBrewerGradientMap;
import cdr.geometry.primitives.ArrayPoint3D;
import cdr.geometry.primitives.ArrayVector3D;
import cdr.geometry.primitives.Circle3D;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Plane3DImpl;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.joglFramework.camera.GLCamera;
import color.VisibilityInteriorsColourMaps;
import evaluations.VisibilityInteriorsEvaluation;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.projection.types.ProjectonPolyhedron;
import math.ValueMapper;
import model.VisibilityInteriorsModel;
import model.connection.Connection;
import model.location.VisibilityInteriorsLocation;
import model.zone.Zone;

public class VisibilityInteriorsLocationRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	
	Float minLineWidth = 1f;
	Float maxLineWidth = 8f;
	
	Float modifiablePointScaleOuter = 10f;
	Float modifiablePointScaleInner = 6f;
	
	Float fixedPointScaleOuter = 8f;
	Float fixedPointScaleInner = 5f;
			
	public void renderEvaluationSinkLocations(GL2 gl, VisibilityInteriorsEvaluation evaluation) {		
		renderSinkLocationsFlow(gl, evaluation, evaluation.getSinkValues(), evaluation.getSinkBounds());	
	}
	
	public void renderEvaluationSinkLocationsZonesFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		renderLocationsZonesFlow(gl, evaluation, evaluation.getSinkValues(), evaluation.getSinkBounds(), true);
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
				
				if (evaluation.getSinkBounds()[0] >= 0 && evaluation.getSinkBounds()[1] <= 1) {
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
				
		renderSourceLocationsFlow(gl, evaluation, values, evaluation.getSourceBounds());
		
	}
	
	public void renderEvaluationSourceLocationsZonesFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation) {
		
		Map<VisibilityInteriorsLocation, Float> values = new HashMap<>();
		
		for (VisibilityInteriorsLocation source : evaluation.getSources()) {
			values.put(source, evaluation.getSourceValue(source));
		}
						
		renderLocationsZonesFlow(gl, evaluation, values, evaluation.getSourceBounds(), false);
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
				
				if (evaluation.getSourceBounds()[0] >= 0 && evaluation.getSourceBounds()[1] <= 1) {
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
				
		for (Connection edge : evaluation.getEdges()) {
			if (evaluation.getEdgeCount(edge) > maxCount) maxCount = evaluation.getEdgeCount(edge);
		}
						
		for (Connection edge : evaluation.getEdges()) {
			
			float lineWidth =( evaluation.getEdgeCount(edge) / maxCount * (maxLineWidth - minLineWidth) ) + minLineWidth;
										
			gl.glLineWidth(lineWidth);		
			gl.glColor3f(0, 0, 0);
									
			geometryRenderer.renderLineSegment3D(gl, edge.getGeometry());	
		}						
	}
		
	public void renderEvaluationZones(GL2 gl, VisibilityInteriorsEvaluation evaluation, VisibilityInteriorsModel m) {
		
		for (Zone zone : m.getZones()) {
			
			float zoneValue = 0f;
			float zoneCount = 0f;
			
			boolean isSink = false;
			
			for (VisibilityInteriorsLocation location : zone.getLocations()) {
				
				Float locationValueSource = evaluation.getSourceValue(location);
				Float locationValueSink = evaluation.getSinkValue(location);
				
				if (locationValueSink != null) {
					zoneValue += locationValueSink;
					zoneCount ++;
					isSink = true;
				
				} else if (locationValueSource != null) {
					zoneValue += locationValueSource;
					zoneCount ++;
				}
			}
			
			if (zoneCount != 0) {
				
				zoneValue /= zoneCount;
				
				float[] bounds = isSink ? evaluation.getSinkBounds() : evaluation.getSourceBounds();
				
				float colorValue = ValueMapper.map(zoneValue, bounds[0], bounds[1], 0, 1);		
			
				Colour c = isSink ? VisibilityInteriorsColourMaps.getSinkColourMap(evaluation).get(colorValue) :  VisibilityInteriorsColourMaps.getSourceColourMap(evaluation).get(colorValue);
				
				gl.glColor3f(c.red(), c.green(), c.blue());

				
				geometryRenderer.renderPolygon3DFill(gl, zone.getGeometry());
			
			} else {
				
				gl.glColor3f(0, 0, 0);
				
				// no render fill;
			}
			
			gl.glColor3f(0f, 0f, 0f);
			gl.glLineWidth(2f);
			
			geometryRenderer.renderPolygon3DLines(gl, zone.getGeometry());		
		}
	}
						
	public void renderSinkLocationsFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation, Map<VisibilityInteriorsLocation, Float> sinks, float[] bounds) {
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> sinkEntry : sinks.entrySet()) {
			
			float value = ValueMapper.map(sinkEntry.getValue(), bounds[0], bounds[1], 0, 1);
			
			Colour c = VisibilityInteriorsColourMaps.getSinkColourMap(evaluation).get(value);
					
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
	
	public void renderSourceLocationsFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation, Map<VisibilityInteriorsLocation, Float> sources, float[] bounds) {
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> sourceEntry : sources.entrySet()) {
			
			float value = ValueMapper.map(sourceEntry.getValue(), bounds[0], bounds[1], 0, 1);
						
			Colour c = VisibilityInteriorsColourMaps.getSourceColourMap(evaluation).get(value);
			
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
	
	public void renderLocationsZonesFlow(GL2 gl, VisibilityInteriorsEvaluation evaluation, Map<VisibilityInteriorsLocation, Float> locations, float[] bounds, boolean isSink) {
		
		for (Map.Entry<VisibilityInteriorsLocation, Float> locationEntry : locations.entrySet()) {
			
			float value = ValueMapper.map(locationEntry.getValue(), bounds[0], bounds[1], 0, 1);
						
			Colour c = isSink ? VisibilityInteriorsColourMaps.getSinkColourMap(evaluation).get(value) : VisibilityInteriorsColourMaps.getSourceColourMap(evaluation).get(value);
						
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
		
		for (List<ProjectonPolyhedron> polyhedrons : location.getProjectionPolyhedra().values()) {
			
			for (ProjectonPolyhedron polyhedron : polyhedrons) {
				for (LayoutGeometry side : polyhedron.getSides()) {
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
	
	public void renderLocationsProjectionPolygons(GL2 gl, Collection<VisibilityInteriorsLocation> locations, VisibilityInteriorsEvaluation evaluation) {
		
		float alpha = 1 / evaluation.getProjectionOverlapBounds()[1];
		
		for (VisibilityInteriorsLocation location : locations) {
			renderLocationProjectionPolygons(gl, location, alpha);
		}
	}
	
	public void renderLocationProjectionPolygons(GL2 gl, VisibilityInteriorsLocation location) {
		renderLocationProjectionPolygons(gl, location, 0.2f);
	}
	
	private void renderLocationProjectionPolygons(GL2 gl, VisibilityInteriorsLocation location, float alpha) {
		
		List<Polygon3DWithHoles> render = new ArrayList<>();
		
		for (List<LayoutGeometry> projections : location.getProjectionPolygons().values()) {
			
			for (LayoutGeometry projection: projections) {
				
				Polygon3DWithHoles renderPolygon = projection.getPolygon3DWithHoles();
				
				renderPolygon.translate(new ArrayVector3D(0, 0, 0.01f));
				
				render.add(renderPolygon);
			}
		}
		
		gl.glColor4f(1f, 0f, 0f, alpha);
		
		geometryRenderer.renderPolygonsWithHoles3DFill(gl, render);	
		
		gl.glLineWidth(0.1f);
		gl.glColor3f(0f, 0f, 0f);
		
		geometryRenderer.renderPolygonsWithHoles3DLines(gl, render);
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
