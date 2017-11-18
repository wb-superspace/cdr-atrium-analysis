package model.location;

import cdr.geometry.primitives.Point3D;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.generic.layout.Layout;
import model.VisibilityInteriorsModel;
import model.location.VisibilityInteriorsLocation.LocationType;

public class VisibilityInteriorsLocationFactory {

	private VisibilityInteriorsModel model;
	
	public VisibilityInteriorsLocationFactory(VisibilityInteriorsModel model) {
		this.model = model;
	}
	
	public VisibilityInteriorsLocation createLocation(Point3D point, LocationType type, boolean isModifiable) {
		
		Float minz = model.findNextMinLayout(point.z());
		
		if (minz == null) return null;
		
		return createLocation(model.getLayout(minz), point, type, isModifiable);
	}
	
	public VisibilityInteriorsLocation createLocation(Layout<LayoutGeometry> layout, Point3D point, LocationType type, boolean isModifiable) {
		
		VisibilityInteriorsLocation location = new VisibilityInteriorsLocation(point, layout, isModifiable);
		
		location.addType(type);
		
		return location;
	}
}
