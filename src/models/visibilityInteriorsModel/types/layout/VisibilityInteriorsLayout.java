package models.visibilityInteriorsModel.types.layout;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream;

import cdr.geometry.primitives.ArrayVector3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon2D;
import cdr.geometry.primitives.Polygon2DWithHoles;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.interaction.data.insert.PointInsertData;
import cdr.mesh.datastructure.Mesh3D;
import cdr.spatialAnalysis.model.isovistModel.IsovistModel;
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import geometry.PolygonsWithHolesGenerator3d;
import geometry.jts.JtsBufferUtils;
import geometry.polygonSampler.Polygon3dWithHolesGridSamplerRectangular;
import lucy.MoreMeshPrimitives;
import models.isovistProjectionModel.types.IsovistProjectionFilter;
import models.isovistProjectionModel.types.IsovistProjectionGeometryType;
import models.isovistProjectionModel.types.IsovistProjectionLayout;
import models.isovistProjectionModel.types.IsovistProjectionPolygon;

public class VisibilityInteriorsLayout extends IsovistProjectionLayout {
			
	private Polygon3dWithHolesGridSamplerRectangular sampler = new Polygon3dWithHolesGridSamplerRectangular();
	
	private List<Mesh3D> renderFloors = new ArrayList<>();
	private List<Mesh3D> renderWalls = new ArrayList<>();
			
	public List<Point3D> getGrid(float resolution) {
	
		List<Point3D> grid = new ArrayList<>();
		
		sampler.setResolution(resolution);
		
		for (Polygon3DWithHoles pgon : this.buildPolygonsWithHoles(true, true, false, 0, 0)) {
			
			sampler.generatePoints(pgon);
			grid.addAll(sampler.getPoints());
		}
		
		return grid;
	}
	
	public void setRenderMeshes() {
		
		List<Polygon3DWithHoles> renderPolygons = buildPolygonsWithHoles(true, false, false, 0, 0);
		
		List<Mesh3D> renderFloors = new ArrayList<>();
		List<Mesh3D> renderWalls = new ArrayList<>();
		
		float floorExtrusionDistance = 0.3f;
		
		for (Polygon3DWithHoles renderPolygon : renderPolygons) {
			
			Mesh3D renderMesh = MoreMeshPrimitives.makeExtrudedMeshFromPolygon2DWithHoles(renderPolygon.getPolygon2DWithHoles(0, 1), floorExtrusionDistance);
			renderMesh.translate(new ArrayVector3D(0, 0, this.getAnchor().z() - floorExtrusionDistance));
			
			renderFloors.add(renderMesh);
		}
		
		for (Polygon3D wall : this.getGeometry(IsovistProjectionGeometryType.WALL, new IsovistProjectionFilter())) {
			
			Mesh3D renderMesh = MoreMeshPrimitives.makeExtrudedMeshFromPolygon3D(wall, this.getFloorToCeilingHeight() - floorExtrusionDistance);
			renderWalls.add(renderMesh);	
		}
		
		this.renderFloors = renderFloors;
		this.renderWalls = renderWalls;
	}
	
	public List<Mesh3D> getRenderMeshesFloor() {
		return this.renderFloors;
	}
	
	public List<Mesh3D> getRenderMeshesWall() {
		return this.renderWalls;
	}
}
