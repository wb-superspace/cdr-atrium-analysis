package models.visibilityInteriorsModel.types.connection;

import cdr.geometry.primitives.LineSegment3D;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation.LocationType;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocationFactory;

public class VisibilityInteriorsConnectionFactory {
	
	private final VisibilityInteriorsModel model;
	private final VisibilityInteriorsLocationFactory locationFactory;
	
	public VisibilityInteriorsConnectionFactory(VisibilityInteriorsModel model) {
		this.model = model;
		this.locationFactory = new VisibilityInteriorsLocationFactory(model);
	}

	public VisibilityInteriorsConnection createConnection(LineSegment3D geometry, boolean bidirectional) {
							
		VisibilityInteriorsLocation sLocation = locationFactory.createLocation(geometry.getStartPoint(), LocationType.ACCESS, false);
		VisibilityInteriorsLocation eLocation = locationFactory.createLocation(geometry.getEndPoint(), LocationType.ACCESS, false);
		
		if (sLocation != null && eLocation != null) {
			return new VisibilityInteriorsConnection(sLocation, eLocation, true);
		}
		
		return null;
	}
}
