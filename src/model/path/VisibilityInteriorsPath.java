package model.path;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;

import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Polygon3D;
import model.connection.Connection;
import model.location.VisibilityInteriorsLocation;

public class VisibilityInteriorsPath {
	
	private List<Connection> connections = new ArrayList<>();
	
	private List<VisibilityInteriorsLocation> locations = new ArrayList<>();
	
	private boolean isValid = false;
	
	public VisibilityInteriorsPath (List<VisibilityInteriorsLocation> locations) {
		this.locations = locations;
				
		for (int i =0; i<locations.size()-1; i++) {
			connections.add(new Connection(locations.get(i), locations.get(i+1), false));
		}
		
		if (this.locations.size() >= 2) {
			this.isValid = true;
		}
	}
	
	public boolean isValid() {
		return this.isValid;
	}
	
	public List<VisibilityInteriorsLocation> getLocations() {
		return this.locations;
	}
	
	public List<Connection> getConnections() {
		return this.connections;
	}
	
	public VisibilityInteriorsLocation getStartLocation() {
		return this.locations.get(0);
	}
	
	public VisibilityInteriorsLocation getEndLocation() {
		return this.locations.get(this.locations.size()-1);
	}
	
	public float getLength() {
		
		if (!this.isValid) return Float.MAX_VALUE;
		
		float length = 0f;
		
		for (Connection connection : this.getConnections()) {
			length += connection.getGeometry().getLength();
		}
		
		return length;
	}
	
	public float getDistance() {	
		
		if (!this.isValid) return Float.MAX_VALUE;
		
		return this.getStartLocation().getDistance(this.getEndLocation());
	}
	
	public float getAccessibility() {
		
		if (!this.isValid) return 0;
		
		return (float) this.getDistance() / (float) this.getLength();
	}
	
	public List<LineSegment3D> getGeometry() {
		
		List<LineSegment3D> geometry = new ArrayList<>();
		
		for (Connection connection : this.connections) {
			geometry.add(connection.getGeometry());
		}
		
		return geometry;
	}
}
