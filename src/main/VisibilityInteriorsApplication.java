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
import evaluations.VisibilityInteriorsEvaluationFactory;
import javafx.beans.property.SimpleBooleanProperty;
import models.visibilityInteriorsModel.VisibilityInteriorsModelBuilder;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.location.VisibilityInteriorsLocation;
import rendering.VisibilityInteriorsModelRenderer;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;

/*
 * Application
 */
public class VisibilityInteriorsApplication  extends OpaqueRendererWithGUI {

	private GLFramework framework;

	private DXFDocument2 dxf;
		
	private VisibilityInteriorsModel model;
	private VisibilityInteriorsModelBuilder modelFactory = new VisibilityInteriorsModelBuilder();
	private VisibilityInteriorsModelRenderer modelRenderer = new VisibilityInteriorsModelRenderer();
	
	public VisibilityInteriorsEvaluation evaluation = null;
	private VisibilityInteriorsEvaluationFactory evaluationFactory;
		
	public Integer index = null;
	public String label = null;
	
	public boolean singleFloorFilter = false;
	public boolean visibleFilter = false;
	public boolean multipleSinkFilter = false;
	
	public SimpleBooleanProperty update = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty controls = new SimpleBooleanProperty(true);
		
	public float cameraXZ = 2f;
	public float cameraYZ = 8f;
	
	public GLCameraModel cameraModel;
	public GLCameraOblique cameraOblique;
		
	public GLFramework getFramework() {
		return framework;
	}
	
	public VisibilityInteriorsModel getModel() {
		return model;
	}
	
	public VisibilityInteriorsModelRenderer getRenderer() {
		return modelRenderer;
	}
				
	@Override
	protected GLCamera createCamera(GLFramework framework) {		
		
		cameraModel = new GLCameraModel(framework);
		
		cameraOblique = new GLCameraOblique(framework);
		cameraOblique.setXZOffsetRatio(cameraXZ);
		cameraOblique.setYZOffsetRatio(cameraYZ);
		
		GLMultiCamera camera = new GLMultiCamera();
		
		camera.addCamera(cameraModel);
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
					
					modelRenderer.renderPlan = !modelRenderer.renderPlan;
					
				} if (e.getKeyChar() == '~') {
					
					controls.set(!controls.get());
					
				} else if (e.getKeyChar() == 'q') {
				
					modelRenderer.renderEvaluation = !modelRenderer.renderEvaluation;
				
				} else if (e.getKeyChar() == 'x') {
				
					modelRenderer.renderEvaluationNodeSinks = !modelRenderer.renderEvaluationNodeSinks;
				
				} else if (e.getKeyChar() == 'X') {
				
					modelRenderer.renderEvaluationNodeSinkLabels = !modelRenderer.renderEvaluationNodeSinkLabels;
				
				}else if (e.getKeyChar() == 's') {
				
					modelRenderer.renderEvaluationNodeSources = !modelRenderer.renderEvaluationNodeSources;
				
				}else if (e.getKeyChar() == 'S') {
				
					modelRenderer.renderEvaluationNodeSourceLabels = !modelRenderer.renderEvaluationNodeSourceLabels;
				
				} else if (e.getKeyChar() == 'e') {
				
					modelRenderer.renderEvaluationEdges = !modelRenderer.renderEvaluationEdges;
				
				}else if (e.getKeyChar() == 'y') {
				
					modelRenderer.renderConnections = !modelRenderer.renderConnections;
				
				} else if (e.getKeyChar() == 't') {
					
					modelRenderer.renderTransparent = !modelRenderer.renderTransparent;
				
				} else if (e.getKeyChar() == 'v') {
					
					modelRenderer.renderEvaluationVisibilityLines = !modelRenderer.renderEvaluationVisibilityLines;
				
				} else if (e.getKeyChar() == 'p') {
					
					modelRenderer.renderProjectionPolygons = !modelRenderer.renderProjectionPolygons;
				
				} else if (e.getKeyChar() == 'i') {
					
					modelRenderer.renderVisibilityCatchmentPolygons = !modelRenderer.renderVisibilityCatchmentPolygons;
				
				}else if (e.getKeyChar() == 'o') {
					
					modelRenderer.renderProjectionPolyhedra = !modelRenderer.renderProjectionPolyhedra;
				
				} else if (e.getKeyChar() == 'z') {
					
					modelRenderer.renderEvaluationZones = !modelRenderer.renderEvaluationZones;
				
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
			modelRenderer.renderGUI(gl, width, height, getCamera(), model);
		}
	}
	
	public void renderFill(GL2 gl) {
			
		if (model != null) {			
			modelRenderer.renderFill(gl, model);
		}
	}
	
	public void renderLines(GL2 gl) {
		
		if (model != null) {			
			modelRenderer.renderLines(gl, model);
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
	
				modelRenderer.update(this.evaluation);
				
				this.update.set(!this.update.get());
			}
		}
	}
	
	public void setVisibilityCatchment(float catchment) {
				
		model.getLocationsActive().forEach(l -> {
		
		l.getEvaluation().setMaxDistance(catchment);
		l.getEvaluation().evaluate();
		
		});
	}
	
	public void setDistanceCatchment(float catchment) {
		
		model.getLocationsActive().forEach(l -> {
			
			l.getEvaluation().setMaxLength(catchment);
			l.getEvaluation().evaluate();
			
		});
	}
				
	public void build() {
		
		new Thread(new Runnable() {
		    public void run() {
		    	
		    	model = modelFactory.buildModel(dxf);	
		    	
		    	evaluationFactory = new VisibilityInteriorsEvaluationFactory(model);
		    	
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
			
			modelRenderer.update(this.evaluation);
			
			this.update.set(!this.update.get());
			
			evaluationFactory.createEvaluations(
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
