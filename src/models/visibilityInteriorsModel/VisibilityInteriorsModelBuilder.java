package models.visibilityInteriorsModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.corba.se.spi.activation.LocatorPackage.ServerLocation;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polyline3D;
import cdr.graph.create.GraphBuilder;
import cdr.joglFramework.frame.GLFramework;
import geometry.PolygonBounds3D;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsZone;


public class VisibilityInteriorsModelBuilder {
	
	public VisibilityInteriorsModel buildModel(DXFDocument2 dxf) {
				
		System.out.println("building model...");		

		if (dxf == null) {
			return null;
		}
		
		VisibilityInteriorsModel model = new VisibilityInteriorsModel();
		
		addModelLayouts(model, getDXFPolygons("FLOORS", dxf));
		
		addModelGeometry(model, getDXFPolygons("VOIDS", dxf), IsovistProjectionGeometryType.VOID, false);
		addModelGeometry(model, getDXFPolygons("WALLS", dxf), IsovistProjectionGeometryType.WALL, false);
		addModelGeometry(model, getDXFPolygons("SOLIDS", dxf), IsovistProjectionGeometryType.SOLID, true);
				
		addModelLocationsModifiable(model, getDXFPoints("LOCATIONS", dxf), true);
		addModelLocationsAccess(model, getDXFPoints("ACCESS", dxf), true);
		
		addModelConnections(model, getDXFLineSegments("CONNECTIONS", dxf), false);
		addModelZones(model, getDXFPolygons("ZONES", dxf), true);
	
		model.buildGraphsMABase();
		
		VisibilityInteriorsModelTreeBuilder.buildConnectivityShortestPathTrees(model);
		VisibilityInteriorsModelTreeBuilder.buildVisibilityShortestPathTrees(model);
		
		List<Float> floorAnchorValues = new ArrayList<>(model.getLayouts().keySet());
		Float floorToCeilingHeight = 5f;
		
		for (int i = 0; i<floorAnchorValues.size(); i++) {
			
			VisibilityInteriorsLayout layout = model.getLayout(floorAnchorValues.get(i));
			
			if (i != floorAnchorValues.size()-1) {
				floorToCeilingHeight = floorAnchorValues.get(i + 1) - floorAnchorValues.get(i);
			}
			
			layout.setFloorToCeilingHeight(floorToCeilingHeight);
			layout.setRenderMeshes();
		}
		
		model.ref = getDXFPolygons("REF", dxf);
		
		System.out.println("...done");
		
		return model;
	}
				
	private List<Polygon3D> getDXFPolygons(String layer, DXFDocument2 dxf) {
		
		List<Polygon3D> polygons = new ArrayList<>();
		
		dxf.getPolygons3D(layer, polygons);
		
		
		for (Polygon3D pgon : polygons) {

			if (pgon.getPlane3D().c() < 0) {
				pgon.reverseWinding();
			}
		}
				
		return polygons;
	}
	
	private List<LineSegment3D> getDXFLineSegments(String layer, DXFDocument2 dxf) {
		
		List <LineSegment3D> lineSegments = new ArrayList<>();
		List <Polyline3D> polylines = new ArrayList<>();
		
		dxf.getLineSegments3D(layer, lineSegments);
		dxf.getPolylines3D(layer, polylines);
		
		for (Polyline3D polyline : polylines) {
			for (LineSegment3D lineSegment : polyline.iterableEdges()) {
				lineSegments.add(lineSegment);
			}
		}
		
		return lineSegments;
	}
	
	private List<Point3D> getDXFPoints(String layer, DXFDocument2 dxf) {
		
		List<Point3D> points = new ArrayList<>();
		
		dxf.getPoints3D(layer, points);
		
		return points;
	}
	
	private void addModelLocationsModifiable(VisibilityInteriorsModel model, List<Point3D> locations, boolean findNextMin) {
		
		for (Point3D location : locations) {
						
			VisibilityInteriorsLayout layout = model.findModelNextMinLayout(location.z());
			
			if (layout != null) {			
				model.addLocation(new VisibilityInteriorsLocation(location, layout, true, false));
			}
		}		
	}
	
	private void addModelLocationsAccess(VisibilityInteriorsModel model, List<Point3D> locations, boolean findNextMin) {
		
		for (Point3D location : locations) {
						
			VisibilityInteriorsLayout layout = model.findModelNextMinLayout(location.z());
			
			if (layout != null) {			
				model.addLocation(new VisibilityInteriorsLocation(location, layout, true, true));
			}
		}		
	}
	
	private void addModelConnections(VisibilityInteriorsModel model, List<LineSegment3D> connections, boolean findNextMin) {
		
		for (LineSegment3D connection : connections) {
			
			VisibilityInteriorsLayout sLayout = model.findModelNextMinLayout(connection.getStartPoint().z());
			VisibilityInteriorsLayout eLayout = model.findModelNextMinLayout(connection.getEndPoint().z());
			
			if (sLayout != null && eLayout != null) {
				
			
				VisibilityInteriorsLocation sLocation = new VisibilityInteriorsLocation(connection.getStartPoint(), sLayout, false, false);
				VisibilityInteriorsLocation eLocation = new VisibilityInteriorsLocation(connection.getEndPoint(), eLayout, false, false);
				
				model.addLocation(sLocation);
				model.addLocation(eLocation);
				
				model.addConnection(new VisibilityInteriorsConnection(sLocation, eLocation, true));
			}
		}
	}
	
	private void addModelZones(VisibilityInteriorsModel model, List<Polygon3D> zones, boolean findNextmin) {
		
		for (Polygon3D zone : zones) {
			
			List<VisibilityInteriorsLocation> locations = new ArrayList<>();
			
			for (Point3D point : zone.iterablePoints()) {
				
				VisibilityInteriorsLayout layout = model.findModelNextMinLayout(point.z());
				
				if (layout != null) {
					
					VisibilityInteriorsLocation location = new VisibilityInteriorsLocation(point, layout, true, false);
					
					model.addLocation(location);
					locations.add(location);
				}
			}
			
			model.addZone(new VisibilityInteriorsZone(locations));
		}
		
		addModelGeometry(model, zones, IsovistProjectionGeometryType.ZONE, true);
	}
			
	private void addModelLayouts(VisibilityInteriorsModel model, List<Polygon3D> floors) {
				
		for (Polygon3D pgon : floors) {
			
			float key = pgon.getAnchor().z();
			
			if (!model.getLayouts().keySet().contains(key)) {
				
				VisibilityInteriorsLayout layout = new VisibilityInteriorsLayout();
				model.getLayouts().put(key, layout);
			}
			
			model.getLayout(key).addGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionPolygon(pgon));
		}
	}

	private void addModelGeometry(VisibilityInteriorsModel model, List<Polygon3D> geometry, IsovistProjectionGeometryType type, boolean findNextMin) {
				
		for (Polygon3D pgon : geometry) {
			
			PolygonBounds3D bounds = new PolygonBounds3D(pgon);

			List<VisibilityInteriorsLayout> layouts = model.findModelBoundedLayouts(bounds.getMinZ(), bounds.getMaxZ());
	
			if (layouts.isEmpty() && findNextMin) {
				VisibilityInteriorsLayout layout = model.findModelNextMinLayout(bounds.getMinZ());
				
				if (layout != null) {
					layout.addGeometry(type, new IsovistProjectionPolygon(pgon));
				}
				
			} else {		
				for (VisibilityInteriorsLayout l : layouts) {
					l.addGeometry(type, new IsovistProjectionPolygon(pgon));
				}
			}
		}
	}
}
