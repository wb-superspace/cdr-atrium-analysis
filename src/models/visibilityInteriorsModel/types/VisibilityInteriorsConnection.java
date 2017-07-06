package models.visibilityInteriorsModel.types;

import java.util.Arrays;
import java.util.List;

import cdr.geometry.primitives.LineSegment3D;

public class VisibilityInteriorsConnection {

	private final VisibilityInteriorsLocation start;
	private final VisibilityInteriorsLocation end;
	
	private final boolean bidirectional;
	
	public VisibilityInteriorsConnection(VisibilityInteriorsLocation start, VisibilityInteriorsLocation end, boolean bidirectional) {
		
		this.start = start;
		this.end = end;
		
		this.bidirectional = bidirectional;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof VisibilityInteriorsConnection) {
			
			VisibilityInteriorsConnection other = (VisibilityInteriorsConnection) obj;
			
			return ((this.start.equals(other.start) && this.end.equals(other.end)) ||
					(this.start.equals(other.end) && this.end.equals(other.start))); 
		}
		
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.start.hashCode() + this.end.hashCode();
	}

	public boolean isBidirectional() {
		return this.bidirectional;
	}
	
	public LineSegment3D getGeometry() {
		return new LineSegment3D(start, end);
	}
	
	public List<VisibilityInteriorsLocation> getLocations() {
		return Arrays.asList(this.start, this.end);
	}
	
	public VisibilityInteriorsLocation getStartLocation() {
		return this.start;
	}
	
	public VisibilityInteriorsLocation getEndLocation() {
		return this.end;
	}
}
