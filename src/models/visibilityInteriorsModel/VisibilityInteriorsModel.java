package models.visibilityInteriorsModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.BidiMap;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.geometry.primitives.LineSegment3D;
import cdr.graph.datastructure.GraphVertex;
import cdr.graph.datastructure.euclidean.Graph3D;
import models.isovistProjectionModel3d.IsovistProjectionLocation;
import templates.Model;


public class VisibilityInteriorsModel implements Model {
	
	private DXFDocument2 dxf;
		
	private SortedMap<Float, VisibilityInteriorsLayout> layouts = new TreeMap<>();
	
	private List<LineSegment3D> connections = new ArrayList<>();
	
	private Float resolution = 1f;

	private float floorToCeilingHeight = 5f;
				
	public VisibilityInteriorsLayout getLayout(Float key) {
		return this.layouts.get(key);
	}
	
	public VisibilityInteriorsLayout findModelNextMinLayout(float z) {
		
		VisibilityInteriorsLayout layout = null;
		
		for (Map.Entry<Float, VisibilityInteriorsLayout> entry : this.getLayouts().entrySet()) {
			
			if (entry.getKey() > z) {
				break;
			}
			
			layout = entry.getValue();
		}
		
		return layout;
	}
	
	public List<VisibilityInteriorsLayout> findModelBoundedLayouts(float zMin, float zMax) {
		
		List<VisibilityInteriorsLayout> layouts = new ArrayList<>();
		
		for (Map.Entry<Float, VisibilityInteriorsLayout> entry : this.getLayouts().entrySet()) {
			
			if (entry.getKey() < zMin) {
				continue;
			} else if (entry.getKey() > zMax) {
				break;
			}
			
			layouts.add(entry.getValue());
		}
				
		return layouts;
	}
	
	public SortedMap<Float, VisibilityInteriorsLayout> getLayouts() {
		return this.layouts;
	}
	
	public List<LineSegment3D> getConnections() {
		return this.connections;
	}
	
	public void setConnections(List<LineSegment3D> connections) {
		this.connections = connections;
	}

	public void setDXF(DXFDocument2 dxf) {
		this.dxf = dxf;
	}
	
	public DXFDocument2 getDXF() {
		return this.dxf;
	}
			
	public Float getResolution() {
		return resolution;
	}

	public void setResolution(Float resolution) {
		this.resolution = resolution;
	}
	
	public float getFloorToCeilingHeight() {
		return floorToCeilingHeight;
	}

	public void setFloorToCeilingHeight(float floorToCeilingHeight) {
		this.floorToCeilingHeight = floorToCeilingHeight;
	}
	

}


