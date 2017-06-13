package models.visibilityInteriorsModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.text.StyleContext.SmallAttributeSet;

import com.sun.crypto.provider.RSACipher;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.geometry.primitives.LineSegment3D;
import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.primitives.Polyline3D;
import cdr.graph.create.GraphBuilder;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.graph.datastructure.vertexEdgeGraph.euclidean.VEGraph3D;
import cdr.joglFramework.frame.GLFramework;
import geometry.PolygonBounds3D;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;


public class VisibilityInteriorsModelBuilder {
	
	public VisibilityInteriorsModel buildModel(GLFramework framework, DXFDocument2 dxf) {
				
		System.out.println("building model...");		

		if (dxf == null) {
			return null;
		}
		
		VisibilityInteriorsModel model = new VisibilityInteriorsModel();
		
		buildModelLayouts(model, getDXFPolygons3D("FLOOR", dxf));
		addModelLayoutGeometry(model, getDXFPolygons3D("VOID", dxf), IsovistProjectionGeometryType.VOID, false);
		addModelLayoutGeometry(model, getDXFPolygons3D("WALL", dxf), IsovistProjectionGeometryType.WALL, false);
		addModelLayoutGeometry(model, getDXFPolygons3D("SOLID", dxf), IsovistProjectionGeometryType.SOLID, true);
		
		addModelConnectivityGraph(model, getDXFLineSegments3D("GRAPH", dxf), true);
				
		System.out.println("...done");
		
		return model;
	}
				
	private List<Polygon3D> getDXFPolygons3D(String layer, DXFDocument2 dxf) {
		
		List<Polygon3D> geometry = new ArrayList<>();
		
		dxf.getPolygons3D(layer, geometry);
				
		return geometry;
	}
	
	private List<LineSegment3D> getDXFLineSegments3D(String layer, DXFDocument2 dxf) {
		
		List <LineSegment3D> lineSegments = new ArrayList<>();
		
		dxf.getLineSegments3D(layer, lineSegments);
		
		return lineSegments;
	}
		
	private void buildModelLayouts(VisibilityInteriorsModel model, List<Polygon3D> floors) {
				
		for (Polygon3D pgon : floors) {
			
			float key = pgon.getAnchor().z();
			
			if (!model.getLayouts().keySet().contains(key)) {
				
				VisibilityInteriorsLayout layout = new VisibilityInteriorsLayout();
				model.getLayouts().put(key, layout);
			}
			
			model.getLayout(key).addGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionPolygon(pgon));
		}
	}

	private void addModelLayoutGeometry(VisibilityInteriorsModel model, List<Polygon3D> geometry, IsovistProjectionGeometryType type, boolean findNextMin) {
				
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
	
	private void addModelConnectivityGraph(VisibilityInteriorsModel model, List<LineSegment3D> edges, boolean findNextMin) {
		
		model.setConnections(edges);
	}
}
