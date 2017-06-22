package main;

import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.joglFramework.camera.GLCamera;
import cdr.joglFramework.camera.GLCameraAxonometric;
import cdr.joglFramework.camera.GLCameraOblique;
import cdr.joglFramework.camera.GLCameraPlan;
import cdr.joglFramework.camera.GLMultiCamera;
import cdr.joglFramework.event.KeyEvent;
import cdr.joglFramework.event.listener.impl.SimpleKeyListener;
import cdr.joglFramework.frame.GLFramework;
import cdr.joglFramework.renderer.OpaqueRendererWithGUI;
import evaluations.VisibilityInteriorsEvaluation;
import javafx.beans.property.SimpleIntegerProperty;
import models.visibilityInteriorsModel.VisibilityInteriorsModelBuilder;
import models.visibilityInteriorsModel.VisibilityInteriorsModelTreeBuilder;
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
		
		GLCamera camera = new GLCameraAxonometric(framework);
		
//		GLCameraOblique camera = new GLCameraOblique(framework);
//		l
//		camera.setXZOffsetRatio(1f);
//		camera.setYZOffsetRatio(4f);
				
        return camera;
	}
			
	@Override
	public void initialiseRenderer(GLFramework framework) {	
		super.initialiseRenderer(framework);
		this.framework = framework; 
		
		framework.getKeyListeners().add(new SimpleKeyListener() {
			
			public void keyTyped(KeyEvent e) {			
				
				int num = Character.getNumericValue(e.getKeyChar());
								
				if(e.getKeyChar() == 'd') {
					
					if (renderer.locationIndex == null || renderer.locationIndex == model.getLocations().size() - 1) {
						renderer.locationIndex = 0;
					} else {
						renderer.locationIndex++;
					}
				
				} else if (e.getKeyChar() == 'n') {
				
					renderer.toggleNodeGraphView = !renderer.toggleNodeGraphView;
				
				} else if (e.getKeyChar() == 'e') {
				
					renderer.renderEvaluation = !renderer.renderEvaluation;
				
				} else if (e.getKeyChar() == 't') {
					
					renderer.renderTransparent = !renderer.renderTransparent;
				
				} else if (e.getKeyChar() == 'v') {
					
					renderer.renderVisibilityLines = !renderer.renderVisibilityLines;
				
				}else if (e.getKeyChar() == 'p') {
					
					renderer.renderProjectionPolygons = !renderer.renderProjectionPolygons;
				
				}else if (e.getKeyChar() == 'g') {
					
					renderer.renderConnectivityGraph = !renderer.renderConnectivityGraph;
				
				} else if (e.getKeyChar() == 'o') {
					
					renderer.renderProjectionPolyhedra = !renderer.renderProjectionPolyhedra;
				
				} else if (e.getKeyChar() == 'l') {
					
					renderer.renderLabels = !renderer.renderLabels;
				
				}else if(e.getKeyChar() == 'a') {
					
					if (renderer.locationIndex == null || renderer.locationIndex == 0) {
						renderer.locationIndex = null;
					} else {
						renderer.locationIndex--;
					}
					
				} else if (num <= 9) {	
					
					renderer.evaluationIndex = num;
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
				
	public void build() {
		
		new Thread(new Runnable() {
		    public void run() {
		    	model = builder.buildModel(framework, dxf);	
		    	model.evaluateLocations();
		    }
		}).start();
	}
	
	public void evaluate() {
		
		if (model != null) {
			
			// TODO - moved to build()
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
