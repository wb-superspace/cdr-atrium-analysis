package models.visibilityInteriorsModel.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cdr.geometry.primitives.Polygon3D;

public class VisibilityInteriorsZone {
	
	private List<VisibilityInteriorsLocation> locations = new ArrayList<>();
	
	private Polygon3D geometry;
	
	public VisibilityInteriorsZone (List<VisibilityInteriorsLocation> locations) {
		this.locations = locations;
		this.geometry = new Polygon3D(locations);
	}
	
	private VisibilityInteriorsZone(List<VisibilityInteriorsLocation> locations, Polygon3D geometry) {
		this.locations = locations;
		this.geometry = geometry;
	}
	
	public Polygon3D getGeometry() {
		return this.geometry;
	}
	
	public List<VisibilityInteriorsLocation> getLocations() {
		return this.locations;
	}
	
	public static VisibilityInteriorsZone fromLocation(VisibilityInteriorsLocation location, Polygon3D geometry) {
		return new VisibilityInteriorsZone(Arrays.asList(location), geometry);
	}
	
	public static VisibilityInteriorsZone fromLocations(List<VisibilityInteriorsLocation> locations, Polygon3D geometry) {
		return new VisibilityInteriorsZone(locations, geometry);
	}
}
