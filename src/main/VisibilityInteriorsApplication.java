package main;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.joglFramework.camera.GLCamera;
import cdr.joglFramework.camera.GLCameraModel;
import cdr.joglFramework.camera.GLCameraOblique;
import cdr.joglFramework.camera.GLMultiCamera;
import cdr.joglFramework.event.KeyEvent;
import cdr.joglFramework.event.listener.impl.SimpleKeyListener;
import cdr.joglFramework.frame.GLFramework;
import cdr.joglFramework.renderer.OpaqueRendererWithGUI;
import cdr.mesh.datastructure.Mesh3D;
import cdr.spacepartition.boundingObjects.BoundingBox3D;
import evaluations.VisibilityInteriorsEvaluation;
import evaluations.VisibilityInteriorsEvaluation.EvaluationType;
import javafx.beans.property.SimpleBooleanProperty;
import models.visibilityInteriorsModel.VisibilityInteriorsModelBuilder;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import rendering.VisibilityInteriorsModelRenderer;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;

/*
 * Application
 */
public class VisibilityInteriorsApplication  extends OpaqueRendererWithGUI {

	private GLFramework framework;

	private DXFDocument2 dxf;
		
	private VisibilityInteriorsModel model;
	private VisibilityInteriorsModelBuilder builder = new VisibilityInteriorsModelBuilder();
	private VisibilityInteriorsModelRenderer renderer = new VisibilityInteriorsModelRenderer();
	
	public Integer index = null;
	public String label = null;
	
	public boolean singleFloorFilter = false;
	public boolean visibleFilter = false;
	public boolean multipleSinkFilter = false;
	
	public SimpleBooleanProperty update = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty controls = new SimpleBooleanProperty(true);
	
	public VisibilityInteriorsEvaluation evaluation = null;
		
	public GLFramework getFramework() {
		return framework;
	}
	
	public VisibilityInteriorsModel getModel() {
		return model;
	}
	
	public VisibilityInteriorsModelRenderer getRenderer() {
		return renderer;
	}
				
	@Override
	protected GLCamera createCamera(GLFramework framework) {		
		
		GLCamera cameraPersp = new GLCameraModel(framework);
		
		GLCameraOblique cameraOblique = new GLCameraOblique(framework);
		cameraOblique.setXZOffsetRatio(2f);
		cameraOblique.setYZOffsetRatio(8f);
		
		GLMultiCamera camera = new GLMultiCamera();
		
		camera.addCamera(cameraPersp);
		camera.addCamera(cameraOblique);
				
        return camera;
	}
			
	@Override
	public void initialiseRenderer(GLFramework framework) {	
		super.initialiseRenderer(framework);
		this.framework = framework; 
		
		framework.getKeyListeners().add(new SimpleKeyListener() {
			
			public void keyTyped(KeyEvent e) {
												
				if (e.getKeyChar() == 'c') {
					
					((GLMultiCamera) getCamera()).nextCamera();
					
					renderer.renderPlan = !renderer.renderPlan;
					
				} if (e.getKeyChar() == '~') {
					
					controls.set(!controls.get());
					
				} else if (e.getKeyChar() == 'q') {
				
					renderer.renderEvaluation = !renderer.renderEvaluation;
				
				} else if (e.getKeyChar() == 'x') {
				
					renderer.renderEvaluationNodeSinks = !renderer.renderEvaluationNodeSinks;
				
				} else if (e.getKeyChar() == 'X') {
				
					renderer.renderEvaluationNodeSinkLabels = !renderer.renderEvaluationNodeSinkLabels;
				
				}else if (e.getKeyChar() == 's') {
				
					renderer.renderEvaluationNodeSources = !renderer.renderEvaluationNodeSources;
				
				}else if (e.getKeyChar() == 'S') {
				
					renderer.renderEvaluationNodeSourceLabels = !renderer.renderEvaluationNodeSourceLabels;
				
				} else if (e.getKeyChar() == 'e') {
				
					renderer.renderEvaluationEdges = !renderer.renderEvaluationEdges;
				
				} else if (e.getKeyChar() == 't') {
					
					renderer.renderTransparent = !renderer.renderTransparent;
				
				} else if (e.getKeyChar() == 'v') {
					
					renderer.renderEvaluationVisibilityLines = !renderer.renderEvaluationVisibilityLines;
				
				} else if (e.getKeyChar() == 'p') {
					
					renderer.renderProjectionPolygons = !renderer.renderProjectionPolygons;
				
				}else if (e.getKeyChar() == 'g') {
					
					renderer.renderConnectivityGraph = !renderer.renderConnectivityGraph;
				
				} else if (e.getKeyChar() == 'o') {
					
					renderer.renderProjectionPolyhedra = !renderer.renderProjectionPolyhedra;
				
				} else if (e.getKeyChar() == 'z') {
					
					renderer.renderEvaluationZones = !renderer.renderEvaluationZones;
				
				} else if(e.getKeyChar() == 'd') {
					
					if (index == null || index == model.getLocationsActive().size() - 1) {
						
						index = 0;
						
					} else {
						
						index++;
					}
					
					setEvaluation();
				
				} else if(e.getKeyChar() == 'a') {
					
					if (index != null) {
						
						if (index == 0) {
							
							index = null;
							
						} else {
							
							index--;
						}
					}
					
					setEvaluation();
				}
				
				e.consume();	
			}
			
		});
	}
	
	@Override
	protected void renderGUI(GL2 gl, int width, int height) {
				
		if (model != null) {			
			renderer.renderGUI(gl, width, height, getCamera(), model);
		}
	}
	
	public void renderFill(GL2 gl) {
			
		if (model != null) {			
			renderer.renderFill(gl, model);
		}
	}
	
	public void renderLines(GL2 gl) {
		
		if (model != null) {			
			renderer.renderLines(gl, model);
		}
	}
		
	public void setEvaluation() {
		
		if (model != null) {
			
			String label = null;
			
			List<VisibilityInteriorsEvaluation> evaluations = new ArrayList<>();
			
			if (index == null) {
				for (VisibilityInteriorsLocation location : model.getLocationsActive()) {
					evaluations.add(location.getEvaluation());				
					label = location.getEvaluation().getLabel();
				}
			} else {
				
				List<VisibilityInteriorsLocation> active = model.getLocationsActive();
				
				evaluations.add(active.get(index).getEvaluation());
				label = active.get(index).getEvaluation().getLabel();
			}
						
			if (label != null) {
				
				this.label = label;
				this.evaluation = VisibilityInteriorsEvaluation.mergeEvaluations(label, evaluations);
	
				renderer.update(this.evaluation);
				
				this.update.set(!this.update.get());
			}
		}
	}
				
	public void build() {
		
		new Thread(new Runnable() {
		    public void run() {
		    	
		    	model = builder.buildModel(dxf);	
		    	
		    	BoundingBox3D bb = new BoundingBox3D();
		    	
		    	for (VisibilityInteriorsLayout l : model.getLayouts().values()) {
		    		for (Mesh3D m : l.getRenderMeshesFloor()) {
		    			bb.add(m);
		    		}
		    	}
		    	
		    	((GLMultiCamera) getCamera()).iterableCameras().forEach(c -> c.zoomExtents(bb));
		    	
		    	  	
		    }
		}).start();
	}
	
	public void evaluate(List<VisibilityInteriorsLocation.LocationType> sinks, List<VisibilityInteriorsLocation.LocationType> sources, EvaluationType type) {
		
		if (model != null) {
						
			index = null;
			
			this.evaluation = null;
			
			renderer.update(this.evaluation);
			
			this.update.set(!this.update.get());
			
			model.buildEvaluations(
					model.getLocationsTypes(sinks),
					model.getLocationsTypes(sources),
					type,
					visibleFilter,
					singleFloorFilter,
					multipleSinkFilter, "Legend");
	
			new Thread(new Runnable() {
			    public void run() {

					for (VisibilityInteriorsLocation location : model.getLocationsActive()) {
						
						location.getEvaluation().evaluate();

						setEvaluation();
					} 
					
					//setEvaluation();
			    }
			}).start();
			
		}
	}
		
	public void clear() {
		
		model = null;
	}
	
	public DXFDocument2 getDXF() {
		return dxf;
	}
	
	public void setDXF(DXFDocument2 dxf) {
		this.dxf = dxf;
	}
}
