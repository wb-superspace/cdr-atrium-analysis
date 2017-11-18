package model.connection;

import cdr.geometry.primitives.LineSegment3D;
import model.VisibilityInteriorsModel;
import model.location.VisibilityInteriorsLocation;
import model.location.VisibilityInteriorsLocationFactory;
import model.location.VisibilityInteriorsLocation.LocationType;

public class ConnectionFactory {
	
	private final VisibilityInteriorsModel model;
	private final VisibilityInteriorsLocationFactory locationFactory;
	
	public ConnectionFactory(VisibilityInteriorsModel model) {
		this.model = model;
		this.locationFactory = new VisibilityInteriorsLocationFactory(model);
	}

	public Connection createConnection(LineSegment3D geometry, boolean bidirectional) {
							
		VisibilityInteriorsLocation sLocation = locationFactory.createLocation(geometry.getStartPoint(), LocationType.ACCESS, false);
		VisibilityInteriorsLocation eLocation = locationFactory.createLocation(geometry.getEndPoint(), LocationType.ACCESS, false);
		
		if (sLocation != null && eLocation != null) {
			return new Connection(sLocation, eLocation, true);
		}
		
		return null;
	}
}
