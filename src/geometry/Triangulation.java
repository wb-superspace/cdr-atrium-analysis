package geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import cdr.geometry.primitives.ArrayPoint3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon2D;
import cdr.geometry.primitives.Polygon2DWithHoles;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polygon3DWithHoles;
import cdr.geometry.primitives.Triangle3D;
import cdr.geometry.toolkit.glu.booleans.GLUPolygonsSubtraction;
import cdr.geometry.toolkit.intersect.GeometryIntersectionTester;
import cdr.geometry.toolkit.intersect.results.PolygonPolygon2DIntersection;
import geometry.jts.JtsDelaunayTriangulator3d;
import geometry.jts.JtsUtils;
import models.isovistProjectionModel.types.IsovistProjectionPolygon;

public class Triangulation {

	public static List<Polygon3D> delauney(List<Point3D> points, Polygon3DWithHoles boundary) {
		
		List<Polygon3D> polygons = new ArrayList<>();
				
		List<Point3D> toTriangulate = new ArrayList<>(points);
		boundary.iterableContours().forEach(p -> p.iterablePoints().forEach(toTriangulate :: add));
		
		List<Triangle3D> triangles = triangulateConstrained(toTriangulate, boundary);
		
		for (Triangle3D t : triangles) {
			if (boundary.isInside(t.getCenter(null))) {
				polygons.add(t.getPolygon3D());
			}
		}
		
		return polygons;
		
	}
		
	private static List<Triangle3D> triangulateConstrained(List<Point3D> points, Polygon3DWithHoles boundary) { 
		
		DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder() ;
		
		builder.setSites(JtsUtils.asJTSCoordinateList(points)); 
		//builder.setSites(JtsUtils.asJTSMultiPolygon(Arrays.asList(boundary.getPolygon2DWithHoles(0, 1))));


		//Populate result with point in polygon test --- 
		ArrayList<Triangle3D> result = new ArrayList<>(); 
		
		Geometry geometry = builder.getTriangles(new GeometryFactory()) ;
		
		for(int i = 0; i < geometry.getNumGeometries(); i++) {
			
			Geometry triangle = geometry.getGeometryN(i) ;

			Coordinate [] coords = triangle.getCoordinates(); 
			
			if(coords.length == 4) {
				
				Triangle3D t = JtsUtils.asGeometryTriangle3D(coords); 
				result.add(t); 
			}
		}

		return result; 
	}
	
	public static Map<Point3D, Polygon3D> voronoi(List<Point3D> points, Polygon3DWithHoles boundary) {
		
		Map<Point3D, Polygon3D> mapped = new HashMap<>();
		
		List<Point3D> toTriangulate = new ArrayList<>(points);
		//boundary.iterableContours().forEach(p -> p.iterablePoints().forEach(toTriangulate :: add));
		
		mapped = voronoiConstrained(toTriangulate, boundary);
		
		return mapped;
	}
	
	private static Map<Point3D, Polygon3D> voronoiConstrained(List<Point3D> points, Polygon3DWithHoles boundary) { 
		
		GeometryIntersectionTester gx = new GeometryIntersectionTester();
		GLUPolygonsSubtraction gs = new GLUPolygonsSubtraction();
		PolygonIntersecter ga = new PolygonIntersecter();
		VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder() ;
		
		float z = ((Point3D) boundary.getAnchor()).z();
		
		builder.setSites(JtsUtils.asJTSCoordinateList(points)); 
		
		Envelope envelope = new Envelope();
		for (Point3D vertex : boundary.getOuterContour().iterablePoints()) {
			envelope.expandToInclude(vertex.x(), vertex.y());
		}
		
		builder.setClipEnvelope(envelope);
		
		//Populate result with point in polygon test --- 
		ArrayList<Polygon3D> result = new ArrayList<>(); 
		ArrayList<Polygon3D> intersected = new ArrayList<>();
		ArrayList<Polygon3D> substracted = new ArrayList<>();
		
		Map<Point3D, Polygon3D> ret = new HashMap<>();
		
		Geometry geometry = builder.getDiagram(new GeometryFactory()) ;
		
		for(int i = 0; i < geometry.getNumGeometries(); i++) {
			
			Geometry pgon = geometry.getGeometryN(i) ;
			
			List<Point3D> pts = new ArrayList<>();
			
			for (Coordinate c : pgon.getCoordinates()) {
				
				Point3D p = JtsUtils.asGeometryPoint3D(c);
				
				pts.add(new ArrayPoint3D(p.x(), p.y(), z));
			}
			
			System.out.println();
			for (Point3D p : pts) System.out.println(p);
			
			result.add(new Polygon3D(pts));
		}
		
		for (Polygon3D res : result) {
			
			PolygonPolygon2DIntersection intersection = gx.intersect(
					res.getPolygon2D(0, 1), 
					boundary.getOuterContour().getPolygon2D(0, 1),
					null);
			
			if (intersection.polygonAInB()) {				
				intersected.add(res);
				continue;
			}
							
			if (intersection.intersectingBoundaries()) {															
				for (Polygon2D px : ga.intersectPolygons(
						res.getPolygon2D(0, 1), 
						boundary.getOuterContour().getPolygon2D(0, 1))) {
					
					intersected.add(px.getPolygon3D(res.getPlane3D()));
				}
			}
		}
		
		for (Polygon3D pgon : intersected) {
			
			List<Polygon2D> subtractors = new ArrayList<>();
			
			for (Polygon3D innerContour : boundary.iterableInnerContours()) {							
				subtractors.add(innerContour.getPolygon2D(0, 1));
			}
		
			for (Polygon2DWithHoles sub : gs.subtractPolygon(pgon.getPolygon2D(0, 1), subtractors)) {		
				substracted.add(sub.getPolygon3DWithHoles(pgon.getPlane3D()).getOuterContour());
			}
			
		}
		
		loop:
		for (Point3D point : points) {
			for (Polygon3D sub : substracted) {
				
				if (sub.getPlane3D().c() < 0) {
					sub.reverseWinding();
				}
				
				if (sub.isInside(point)) {
					ret.put(point, sub);
					continue loop;
				}
			}
		}
		
		return ret; 
	}
}
