package models.visibilityInteriorsModel.types.location;

import cdr.geometry.primitives.Point3D;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation.LocationType;

public class VisibilityInteriorsLocationFactory {

	private VisibilityInteriorsModel model;
	
	public VisibilityInteriorsLocationFactory(VisibilityInteriorsModel model) {
		this.model = model;
	}
	
	public VisibilityInteriorsLocation createLocation(Point3D point, LocationType type, boolean isModifiable) {
		
		VisibilityInteriorsLayout layout = model.findModelNextMinLayout(point.z());
		
		if (layout == null) return null;
		
		return createLocation(layout, point, type, isModifiable);
	}
	
	public VisibilityInteriorsLocation createLocation(VisibilityInteriorsLayout layout, Point3D point, LocationType type, boolean isModifiable) {
		
		VisibilityInteriorsLocation location = new VisibilityInteriorsLocation(point, layout, isModifiable);
		
		location.addType(type);
		
		return location;
	}
}
