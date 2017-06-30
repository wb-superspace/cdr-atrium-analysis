package models.VisibilityInteriorsModel.types;

import java.util.ArrayList;
import java.util.List;

import cdr.geometry.primitives.Polygon3D;

public class VisibilityInteriorsZone {
	
	private List<VisibilityInteriorsLocation> locations = new ArrayList<>();
	
	private Polygon3D geometry;
	
	public VisibilityInteriorsZone (List<VisibilityInteriorsLocation> locations) {
		this.locations = locations;
		this.geometry = new Polygon3D(locations);
	}
	
	public Polygon3D getGeometry() {
		return this.geometry;
	}
	
	public List<VisibilityInteriorsLocation> getLocations() {
		return this.locations;
	}
}
