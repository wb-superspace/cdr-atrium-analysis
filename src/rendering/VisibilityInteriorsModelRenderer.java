package rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.media.opengl.GL2;

import cdr.geometry.primitives.Polygon3D;
import cdr.geometry.renderer.GeometryRenderer;
import cdr.graph.datastructure.GraphEdge;
import cdr.graph.datastructure.euclidean.Graph3D;
import cdr.joglFramework.camera.GLCamera;
import cdr.mesh.renderer.MeshRenderer3DOutline;
import models.isovistProjectionModel3d.IsovistProjectionFilter;
import models.isovistProjectionModel3d.IsovistProjectionGeometryType;
import models.isovistProjectionModel3d.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;



public class VisibilityInteriorsModelRenderer {

	GeometryRenderer geometryRenderer = new GeometryRenderer();
	MeshRenderer3DOutline meshRenderer = new MeshRenderer3DOutline();
		
	VisibilityInteriorsLocationRenderer locationRenderer = new VisibilityInteriorsLocationRenderer();
		
	public Integer evaluationIndex = 0;
	public Integer locationIndex = null;
	
	public boolean renderVisibilityGraph = false;
	public boolean renderConnectivityGraph = true;
	
	public boolean renderVisibilityLines = false;
	
	public boolean toggleNodeGraphView = false;
	
	public boolean renderProjectionPolygons = false;
	public boolean renderProjectionPolyhedra = false;
	
	public boolean renderEvaluation = true;
	public boolean renderEvaluationLabels = false;
	
	public boolean renderMesh = true;
	public boolean renderTransparent = false;
	public boolean renderLabels = false;
			
	public void renderFill(GL2 gl, VisibilityInteriorsModel m) {

		if (m == null) {
			return;
		}
		
		if (renderMesh && !renderTransparent) {
			this.renderModelLayoutMeshes(gl, m);
		} else {

		}
	}
	
	public void renderLines(GL2 gl, VisibilityInteriorsModel m) {
		
		if (m == null) {
			return;
		}
		
		if (renderMesh && renderTransparent) {
			this.renderModelLayoutMeshes(gl, m);
		} 
						
		this.renderModelConnections(gl, m);
			
		List<VisibilityInteriorsLocation> active = new ArrayList<>();
		
		if (locationIndex == null) {
			
			active.addAll(m.getLocations());
		} else {
			active.add(m.getLocations().get(locationIndex));
		}
		
		if (renderProjectionPolyhedra) {
			locationRenderer.renderLocationsProjectionPolyhedra(gl, active);
		}
		
		if (renderProjectionPolygons) {
			locationRenderer.renderLocationsProjectionPolygons(gl, active);
		}
		
		if (renderConnectivityGraph) {
			this.renderModelConnectivityGraph(gl, m);
		}
		
		if (renderVisibilityGraph) {
			this.renderModelVisibilityGraph(gl, m);
		}
						
		switch (evaluationIndex) {
		
		case 0:
						
			if (renderEvaluation) {
				locationRenderer.renderSinkLocationsValues(gl, m.getLocationValuesVisibility(active));
			}
			
			break;
		
		case 9:
			
			if (renderEvaluation) {
				locationRenderer.renderSinkLocationsValues(gl, m.getLocationValuesExposure(active));
			}
			
			break;
			

		default:
				
			if (renderEvaluation) {
				
				if (toggleNodeGraphView) {
					locationRenderer.renderLocationsEvaluationGraph(gl, active, evaluationIndex - 1);	
				} else {
					locationRenderer.renderLocationsEvaluationNodes(gl, active, evaluationIndex - 1);	
				}
				
				if (renderVisibilityLines) {
					locationRenderer.renderLocationsEvaluationVisibilityGraphPaths(gl, active, evaluationIndex - 1);
				}
			}

					
			break;
		}		
	}
	
	public void renderGUI(GL2 gl, int width, int height, GLCamera cam, VisibilityInteriorsModel m) {
		
		if (m == null) {
			return;
		}	
		
		List<VisibilityInteriorsLocation> active = new ArrayList<>();
		
		if (locationIndex == null) {
			
			active.addAll(m.getLocations());
		} else {
			active.add(m.getLocations().get(locationIndex));
		}
		
		if (renderLabels) {
			
			switch (evaluationIndex) {
			
			case 0:
				
				for (VisibilityInteriorsLocation location : active) {
					
					locationRenderer
						.renderLocationLabel(gl, cam, location,
								Float.toString(m.getLocationValuesVisibility(Arrays.asList(location)).get(location)));			
					
				}

				break;
			
			case 9:
				
				for (VisibilityInteriorsLocation location : active) {
					
					locationRenderer
						.renderLocationLabel(gl, cam, location,
								Float.toString(m.getLocationValuesExposure(Arrays.asList(location)).get(location)));			
					
				}

				break;

			default:
					
				for (VisibilityInteriorsLocation location : active) {
					
					if (location.getEvaluation( evaluationIndex -1 ) != null) {
						locationRenderer
							.renderLocationLabel(gl, cam, location,
									Float.toString(location.getEvaluation( evaluationIndex -1 ).getSinkValue(location)));
						
					}
				}
						
				break;
			}		
		}			
	}
		
	private void renderModelConnectivityGraph(GL2 gl, VisibilityInteriorsModel m) {
		
		if (m.getConnectivityGraph() == null) return;
		
		Graph3D connectivityGraph = m.getConnectivityGraph();
		
		gl.glColor3f(0f, 0f, 0f);
		gl.glLineWidth(0.2f);
		
		for (GraphEdge edge : connectivityGraph.iterableEdges()) {
			geometryRenderer.renderLineSegment3D(gl, connectivityGraph.getEdgeData(edge));
		}
		
	}
	
	private void renderModelVisibilityGraph(GL2 gl, VisibilityInteriorsModel m) {
		
		if (m.getVisibilityGraph() == null) return;
		
		Graph3D visibilityGraph = m.getVisibilityGraph();
		
		gl.glLineWidth(0.1f);
		gl.glColor3f(0.5f,0.6f,0.5f);
		
		for (GraphEdge edge : visibilityGraph.iterableEdges()) {
			geometryRenderer.renderLineSegment3D(gl, visibilityGraph.getEdgeData(edge));
		}
		
	}
			
	private void renderModelConnections(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glLineWidth(1);
		gl.glColor3f(0f,0f,0f);
		
		for (VisibilityInteriorsConnection connection : m.getConnections()) {
			geometryRenderer.renderLineSegment3D(gl, connection.getGeometry());
		}
	}
							
	private void renderModelLayoutsLines(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glLineWidth(0.1f);
		gl.glColor3f(0f,0f,0f);
		
		List<Polygon3D> pgons = new ArrayList<>();
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			pgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.FLOOR, new IsovistProjectionFilter()));
			pgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.VOID, new IsovistProjectionFilter()));
			pgons.addAll(layout.getGeometry(IsovistProjectionGeometryType.SOLID, new IsovistProjectionFilter()));
			
			for (IsovistProjectionPolygon wall : layout.getGeometry(IsovistProjectionGeometryType.WALL, 
					new IsovistProjectionFilter())) {
				
				if (wall.getOuterTypes().isEmpty()) {
					continue;
				}
				
				pgons.add(wall);
			}
		}
		
		geometryRenderer.renderPolygons3DLines(gl, pgons);
	}
	
	private void renderModelLayoutMeshes(GL2 gl, VisibilityInteriorsModel m) {
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
			
			gl.glColor3f(0.8f,0.8f,0.8f);
			
			meshRenderer.renderFill(gl, layout.getRenderMeshesFloor());
			
			gl.glColor3f(0.7f,0.7f,0.7f);
			
			meshRenderer.renderFill(gl, layout.getRenderMeshesWall());
			
			gl.glLineWidth(0.01f);
			gl.glColor3f(0, 0, 0);
										
			meshRenderer.renderCornerEdges(gl, layout.getRenderMeshesFloor(), 0.1f);	
			meshRenderer.renderBoundaryEdges(gl, layout.getRenderMeshesFloor());
			
			meshRenderer.renderCornerEdges(gl, layout.getRenderMeshesWall(), 0.1f);	
			meshRenderer.renderBoundaryEdges(gl, layout.getRenderMeshesWall());
		}
	}
		
	private void renderModelLayoutsWallsFill(GL2 gl, VisibilityInteriorsModel m) {
		
		gl.glColor3f(0.4f,0.4f,0.4f) ;
		
		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {

			for (IsovistProjectionPolygon wall : layout.getGeometry(IsovistProjectionGeometryType.WALL, 
					new IsovistProjectionFilter())) {
				
				if (wall.getOuterTypes().isEmpty()) {
					continue;
				}
				
				wall = new IsovistProjectionPolygon(wall.iterablePoints());
				geometryRenderer.renderPolygon3DFill(gl, wall);
				wall.reverseWinding();
				geometryRenderer.renderPolygon3DFill(gl, wall);
			}
		}
	}
	
	private void renderModelLayoutsFloorsFill(GL2 gl, VisibilityInteriorsModel m) {
		
//		gl.glColor3f(0.8f,0.8f,0.8f);
//		
//		for (VisibilityInteriorsLayout layout : m.getLayouts().values()) {
//
//			for (IsovistProjectionPolygon solid : layout.getGeometry(IsovistProjectionGeometryType.FLOOR, 
//					new IsovistProjectionFilter())) {
//				
//				solid = new IsovistProjectionPolygon(solid);
//				geometryRenderer.renderPolygonsWithHoles3DFill(gl, Arrays.asList(solid.getPolygon3DWithHoles()));
//				solid.reverseWinding();
//				geometryRenderer.renderPolygonsWithHoles3DFill(gl, Arrays.asList(solid.getPolygon3DWithHoles()));
//			}
//		}
	}
}
