package model;

import java.util.ArrayList;
import java.util.List;

import com.sun.xml.internal.bind.v2.runtime.Location;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polyline3D;
import jpantry.geometry.Bounds3D;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.generic.geometry.LayoutGeometryType;
import jpantry.models.generic.layout.Layout;
import model.connection.Connection;
import model.connection.ConnectionFactory;
import model.location.VisibilityInteriorsLocation;
import model.location.VisibilityInteriorsLocation.LocationType;
import model.zone.Zone;
import model.zone.ZoneFactory;


public class VisibilityInteriorsModelBuilder {
	
	public VisibilityInteriorsModel buildModel(DXFDocument2 dxf) {
				
		System.out.println("building model...");		

		if (dxf == null) {
			return null;
		}
		
		VisibilityInteriorsModel model = new VisibilityInteriorsModel();
			
		addModelLayouts(model, getDXFPolygons("FLOORS", dxf));
		
		addModelGeometry(model, getDXFPolygons("VOIDS", dxf), LayoutGeometryType.VOID, false);
		addModelGeometry(model, getDXFPolygons("WALLS", dxf), LayoutGeometryType.WALL, false);
		addModelGeometry(model, getDXFPolygons("SOLIDS", dxf), LayoutGeometryType.SOLID, true);
				
		addModelLocationsModifiable(model, getDXFPoints("UNITS", dxf), LocationType.UNIT);
		addModelLocationsModifiable(model, getDXFPoints("ENTRANCES", dxf), LocationType.ENTRANCE);
		
		addModelConnections(model, getDXFLineSegments("CONNECTIONS", dxf));
		
		addModelZones(model, getDXFPolygons("ZONES", dxf));
	
		VisibilityInteriorsModelGraphBuilder.buildGraphsMABase(model);
		VisibilityInteriorsModelTreeBuilder.buildConnectivityShortestPathTrees(model);
		VisibilityInteriorsModelTreeBuilder.buildVisibilityShortestPathTrees(model);
		
		List<Float> floorAnchorValues = new ArrayList<>(model.getLayouts().keySet());
		Float floorToCeilingHeight = 5f;
		
		for (int i = 0; i<floorAnchorValues.size(); i++) {
			
			Layout<LayoutGeometry> layout = model.getLayout(floorAnchorValues.get(i));
			
			if (i != floorAnchorValues.size()-1) {
				floorToCeilingHeight = floorAnchorValues.get(i + 1) - floorAnchorValues.get(i);
			}
			
			layout.setFloorToCeilingHeight(floorToCeilingHeight);
			layout.setRenderMeshes();
		}
		
		//model.ref = getDXFPolygons("REF", dxf);
		
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
	
	private void addModelLocationsModifiable(VisibilityInteriorsModel model, List<Point3D> points, LocationType type) {
		
		for (Point3D point : points) {
			
			Float minz = model.findNextMinLayout(point.z());
			
			if (minz != null) {		
				
				Layout<LayoutGeometry> layout = model.getLayout(minz);
				
				VisibilityInteriorsLocation location = new VisibilityInteriorsLocation(point, layout, true);
				
				location.addType(type);
				
				model.addLocation(location);
			}
		}		
	}
		
	private void addModelConnections(VisibilityInteriorsModel model, List<LineSegment3D> geometry) {
		
		ConnectionFactory factory = new ConnectionFactory(model);
		
		for (LineSegment3D geo : geometry) {
			
			Connection connection = factory.createConnection(geo, false);
			
			if (connection == null) continue;
			
			model.addConnection(connection);
			
			connection.getLocations().forEach(location -> model.addLocation(location));
		}
	}
	
	private void addModelZones(VisibilityInteriorsModel model, List<Polygon3D> geometry) {
		
		ZoneFactory factory = new ZoneFactory(model);
		
		for (Polygon3D geo : geometry) {						
			
			Zone zone = factory.createZone(geo);
			
			model.addZone(zone);
			
			zone.getLocations().forEach(location -> model.addLocation(location));
		}
		
		addModelGeometry(model, geometry, LayoutGeometryType.ZONE, true);
	}
			
	private void addModelLayouts(VisibilityInteriorsModel model, List<Polygon3D> floors) {
				
		for (Polygon3D pgon : floors) {
			
			float key = pgon.getAnchor().z();
			
			if (!model.getLayouts().keySet().contains(key)) {
								
				Layout<LayoutGeometry> layout = new Layout<>();
				
				model.getLayouts().put(key, layout);
			}
			
			model.getLayout(key).addGeometry(LayoutGeometryType.FLOOR, new LayoutGeometry(pgon));
		}
	}

	private void addModelGeometry(VisibilityInteriorsModel model, List<Polygon3D> geometry, LayoutGeometryType type, boolean findNextMin) {
				
		for (Polygon3D pgon : geometry) {
			
			Bounds3D bounds = new Bounds3D(pgon);

			List<Float> layouts = model.findBoundedLayouts(bounds.getMinZ(), bounds.getMaxZ());
	
			if (layouts.isEmpty() && findNextMin) {
				
				Float minz = model.findNextMinLayout(bounds.getMinZ());
				
				if (minz != null) {
					
					Layout<LayoutGeometry> layout = model.getLayout(minz);
					
					layout.addGeometry(type, new LayoutGeometry(pgon));
				}
				
			} else {		
				for (Float z : layouts) {
					model.getLayout(z).addGeometry(type, new LayoutGeometry(pgon));
				}
			}
		}
	}
}
