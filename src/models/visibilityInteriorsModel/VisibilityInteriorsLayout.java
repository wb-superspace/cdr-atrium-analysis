package models.visibilityInteriorsModel;

import java.util.ArrayList;
import java.util.List;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.interaction.data.insert.PointInsertData;
import cdr.spatialAnalysis.model.isovistModel.IsovistModel;
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import geometry.PolygonsWithHolesGenerator3d;
import geometry.polygonSampler.Polygon3dWithHolesGridSamplerRectangular;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionLayout;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;

public class VisibilityInteriorsLayout extends IsovistProjectionLayout {
	
	private List<Polygon3DWithHoles> visible;
	
	private IsovistModel visibilityModel = new IsovistModel();
	private Polygon3dWithHolesGridSamplerRectangular sampler = new Polygon3dWithHolesGridSamplerRectangular();
	
	public IsovistLocation getIsovist(Point3D pt) {
		
		this.setVisibilityPolygons();
			
		IsovistLocation location =  visibilityModel.addLocation(
				new PointInsertData(pt), true);
		
		if (location == null) {	
			return null;
		}
		
		return location;
	}
	
	public List<Point3D> getGrid(float resolution) {
	
		List<Point3D> grid = new ArrayList<>();
		
		sampler.setResolution(resolution);
		
		for (Polygon3DWithHoles pgon : this.getGridSamplerPolygons()) {
			
			sampler.generatePoints(pgon);
			grid.addAll(sampler.getPoints());
		}
		
		return grid;
	}
	
		
	private List<Polygon3DWithHoles> getGridSamplerPolygons() {
		
		List<Polygon3D> sample = new ArrayList<>();

		for (IsovistProjectionPolygon floor : this.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter())) {
			sample.add(floor);
		}
		
		for (IsovistProjectionPolygon wall : this.getGeometry(IsovistProjectionGeometryType.WALL, new IsovistProjectionFilter()))  {
			
			if (!wall.getInnerTypes().contains(IsovistProjectionGeometryType.FLOOR)) {
				sample.add(wall);
			}
		}
		
		for (IsovistProjectionPolygon voi : this.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter())) {
			sample.add(voi);
		}
				
		return new PolygonsWithHolesGenerator3d().getPolygonsWithHoles(sample);
	}
	
	private void setVisibilityPolygons() {
		
		if (this.visible != null) {	
			return;
		}
		
		this.visible = new ArrayList<>();
		
		List<Polygon3D> visible = new ArrayList<>();
					
		for (IsovistProjectionPolygon floor : this.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter())) {
			
			if (floor.getOuterTypes().contains(IsovistProjectionGeometryType.WALL)) {
				for (IsovistProjectionPolygon wall : floor.getOuterContours(IsovistProjectionGeometryType.WALL)) {
					if (!visible.contains(wall)) {
						visible.add(wall);
					}
				}
				
			} else {
				visible.add(floor);
			}
		}
					
		for (IsovistProjectionPolygon wall : this.getGeometry(IsovistProjectionGeometryType.WALL, new IsovistProjectionFilter())) {
		
			if (!wall.getInnerTypes().contains(IsovistProjectionGeometryType.FLOOR)) {
				visible.add(wall);
			}
		}	
		 	
		this.visible = new PolygonsWithHolesGenerator3d().getPolygonsWithHoles(visible);
		this.visibilityModel.setGeometryPolygons3D(this.visible);
	}
}
