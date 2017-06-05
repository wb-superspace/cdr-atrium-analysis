package models.visibilityInteriorsModel;
import java.util.SortedMap;
import java.util.TreeMap;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.graph.datastructure.euclidean.Graph3D;
import templates.Model;


public class VisibilityInteriorsModel implements Model {
	
	private DXFDocument2 dxf;
		
	private SortedMap<Float, VisibilityInteriorsLayout> layouts = new TreeMap<>();
	
	private Graph3D graph;
	
	private Float resolution = 1f;

	private float floorToCeilingHeight = 5f;
				
	public VisibilityInteriorsLayout getLayout(Float key) {
		return this.layouts.get(key);
	}
	
	public SortedMap<Float, VisibilityInteriorsLayout> getLayouts() {
		return this.layouts;
	}
	
	public Graph3D getGraph() {
		return this.graph;
	}
	
	public void setGraph(Graph3D graph) {
		this.graph = graph;
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


