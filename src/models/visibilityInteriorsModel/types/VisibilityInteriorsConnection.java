package models.visibilityInteriorsModel.types;

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
	
	public boolean equals(VisibilityInteriorsConnection other) {
		
		return ((this.start == other.start && this.end == other.end) ||
				(this.start == other.end && this.end == other.start)); 
	}
	
	public boolean isBidirectional() {
		return this.bidirectional;
	}
	
	public LineSegment3D getGeometry() {
		return new LineSegment3D(start, end);
	}
	
	public VisibilityInteriorsLocation getStartLocation() {
		return this.start;
	}
	
	public VisibilityInteriorsLocation getEndLocation() {
		return this.end;
	}
}
