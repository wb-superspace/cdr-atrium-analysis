package sup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import cdr.colour.HSVColour;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.joglFramework.camera.GLCamera;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;
import models.visibilityInteriorsModel.types.VisibilityInteriorsZone;
import rendering.VisibilityInteriorsLocationRenderer;

public class VisibilityInteriorsDiscoverabilityRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	VisibilityInteriorsLocationRenderer locationRenderer = new VisibilityInteriorsLocationRenderer();
	
	Float minLineWidth = 0.5f;
	Float maxLineWidth = 5f;
	
	public void renderZoneDiscoverability(GL2 gl, VisibilityInteriorsZone zone) {
		
		for (VisibilityInteriorsLocation location : zone.getLocations()) {	
			locationRenderer.renderLocationProjectionPolygons(gl, location);
		}
		
		gl.glColor3f(1f, 1f, 1f);
		geometryRenderer.renderPolygon3DFill(gl, zone.getGeometry());
		
		gl.glColor3f(0f, 0f, 0f);
		gl.glLineWidth(2f);
		
		geometryRenderer.renderPolygon3DLines(gl, zone.getGeometry());
		
		this.renderLocationsDiscoverability(gl, zone.getLocations());
	}
	
	public void renderLocationsDiscoverability(GL2 gl, List<VisibilityInteriorsLocation> locations) {
		
		List<VisibilityInteriorsLocation> sources = new ArrayList<>();
		List<VisibilityInteriorsLocation> visible = new ArrayList<>();
		
		Map<VisibilityInteriorsLocation, VisibilityInteriorsLocation> minDiscoverability = new HashMap<>();
		Map<VisibilityInteriorsLocation, VisibilityInteriorsLocation> minVisibility = new HashMap<>();
		
		for (VisibilityInteriorsLocation location : locations) {
			for (VisibilityInteriorsLocation target : location.getVisibilityPathLocations()) {				
				if (location.getVisibilityPath(target).getLocations().size() == 2){
					if (!visible.contains(target)) {
						visible.add(target);
						
						minVisibility.put(target, location);
					}
				}
			}
		}
		
		for (VisibilityInteriorsLocation location : locations) {
			for (VisibilityInteriorsLocation target : location.getVisibilityPathLocations()) {
				if (!sources.contains(target) && !visible.contains(target)) {
					sources.add(target);
				}
			}
		}
		
		Map<LineSegment3D, Float> edgeCounts = new HashMap<>();
		Map<LineSegment3D, Float> edgeValues = new HashMap<>();
		
		float maxCount = 1f;
		float maxVal = 0f;
		
		for (VisibilityInteriorsLocation source : sources) {
			
			VisibilityInteriorsPath minPath = null;
			VisibilityInteriorsLocation minSink = null;
			
			for (VisibilityInteriorsLocation visibleLocation : visible) {
				
				VisibilityInteriorsPath path = source.getConnectivityPath(visibleLocation);
				
				if (path == null) {			
					continue;
				
				} else {
					
					if (minPath == null || path.getLength() < minPath.getLength()) {
						minPath = path;
						minSink = visibleLocation;
					}
				}
			}
			
			if (minPath == null || minPath.getLength() == 2) {
				continue; // this needs a good / bad value
			}
			
			minDiscoverability.put(source, minSink);
						
			for (VisibilityInteriorsConnection connection : minPath.getConnections()) {
				
				LineSegment3D geo = connection.getGeometry();
				
				if (!edgeCounts.containsKey(geo)) {
					
					edgeCounts.put(geo, 1f);
					
				} else {
					
					float edgeCount = edgeCounts.get(geo) + 1;
					
					if (edgeCount > maxCount) {
						maxCount = edgeCount;
					}
					
					edgeCounts.put(geo, edgeCount);
				}
				
				VisibilityInteriorsLocation connectionLocation = connection.getStartLocation();
				VisibilityInteriorsPath connectionLocationPath = minSink.getConnectivityPath(connectionLocation);
						
				float value = 0f;
				
				if (connectionLocation != minSink) {
					value = connectionLocationPath.getLength();
				}
				
				if (value > maxVal) {
					maxVal = value;
				}
				
				edgeValues.put(geo, value);
			}
		}
		
		for (Map.Entry<LineSegment3D, Float> entry : edgeCounts.entrySet()) {
			
			float lineWidth =( entry.getValue() / maxCount * (maxLineWidth - minLineWidth) ) + minLineWidth;
			float lineValue = edgeValues.get(entry.getKey()) / maxVal;
				
			gl.glLineWidth(lineWidth);
						
			HSVColour c = new HSVColour() ;
						
			c.setHSV(lineValue * 0.6f, 1f, 1f) ;	
			
			gl.glPointSize(5);
			gl.glColor3f(c.red(), c.green(), c.blue());
						
			geometryRenderer.renderLineSegment3D(gl, entry.getKey());	
		}
		
		for (VisibilityInteriorsLocation visibleLocation : visible) {
			
			VisibilityInteriorsPath path = visibleLocation.getVisibilityPath(minVisibility.get(visibleLocation));
			
			if (path == null) {
				continue;
			}
			
			for (VisibilityInteriorsConnection connection : path.getConnections()) {
				
				gl.glColor3f(0.5f, 0.5f, 0.5f);
				gl.glLineWidth(0.2f);
				
				geometryRenderer.renderLineSegment3D(gl, connection.getGeometry());
			}
		}
		
		for (VisibilityInteriorsLocation location : locations) {
			locationRenderer.renderSinkLocations(gl, Arrays.asList(location));
		}

		locationRenderer.renderSourceLocations(gl, sources);	
	}
}
