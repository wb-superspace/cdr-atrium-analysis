package main;

import javax.media.opengl.GL2;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.joglFramework.camera.GLCamera;
import cdr.joglFramework.camera.GLCameraAxonometric;
import cdr.joglFramework.camera.GLCameraPlan;
import cdr.joglFramework.camera.GLMultiCamera;
import cdr.joglFramework.frame.GLFramework;
import cdr.joglFramework.renderer.OpaqueRendererWithGUI;
import models.visibilityInteriorsModel.VisibilityInteriorsModelBuilder;
import models.visibilityInteriorsModel.VisibilityInteriorsModelEvaluator;
import models.visibilityInteriorsModel.VisibilityInteriorsModelExporter;
import models.visibilityInteriorsModel.VisibilityInteriorsModelRenderer;
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
	private VisibilityInteriorsModelEvaluator evaluator = new VisibilityInteriorsModelEvaluator();
	
	VisibilityInteriorsModelExporter exporter = new VisibilityInteriorsModelExporter(); // TODO - make setters
	
	private GLCameraAxonometric cameraAxon;
	private GLCameraPlan cameraPlan;
	
	public enum CameraType {
		PLAN,
		AXON
	}
	
	public GLFramework getFramework() {
		return framework;
	}
	
	public VisibilityInteriorsModel getModel() {
		return model;
	}
	
	public VisibilityInteriorsModelRenderer getRenderer() {
		return renderer;
	}
	
	public VisibilityInteriorsModelEvaluator getEvaluator() {
		return evaluator;
	}
	
	@Override
	protected GLCamera createCamera(GLFramework framework) {		
		
		cameraPlan = new GLCameraPlan(framework) ;
		cameraAxon = new GLCameraAxonometric(framework);
		
		GLMultiCamera multiCamera = new GLMultiCamera();
		multiCamera.addCamera(cameraAxon);
		multiCamera.addCamera(cameraPlan);
        multiCamera.setCamera(cameraAxon);
        
        return multiCamera;
	}
			
	public void setCamera(CameraType cameraType) {
		
		switch (cameraType) {
		case AXON:
			
			((GLMultiCamera) getCamera()).setCamera(cameraAxon);
			break;

		case PLAN:
			((GLMultiCamera) getCamera()).setCamera(cameraPlan);
			break;
			
		default:
			((GLMultiCamera) getCamera()).setCamera(cameraAxon);
			break;
		}
	}

	@Override
	public void initialiseRenderer(GLFramework framework) {	
		super.initialiseRenderer(framework);
		this.framework = framework; 
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
		
	@Override
	protected void renderGUI(GL2 gl, int width, int height) {
				
		if (model != null) {			
			renderer.renderGUI(gl, width, height, getCamera(), model);
		}
	}
		
	public void build() {
		model = builder.buildModel(framework, dxf);	
	}
		
	public void clear() {
		model = null;
		renderer = new VisibilityInteriorsModelRenderer();
		builder = new VisibilityInteriorsModelBuilder();
		evaluator = new VisibilityInteriorsModelEvaluator();
	}
	
	public DXFDocument2 getDXF() {
		return dxf;
	}
	
	public void setDXF(DXFDocument2 dxf) {
		this.dxf = dxf;
	}
}
