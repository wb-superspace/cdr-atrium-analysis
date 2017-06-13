package main;

import javax.media.opengl.GL2;

import cdr.fileIO.dxf2.DXFDocument2;
import cdr.joglFramework.camera.GLCamera;
import cdr.joglFramework.camera.GLCameraAxonometric;
import cdr.joglFramework.camera.GLCameraPlan;
import cdr.joglFramework.camera.GLMultiCamera;
import cdr.joglFramework.event.KeyEvent;
import cdr.joglFramework.event.listener.impl.SimpleKeyListener;
import cdr.joglFramework.frame.GLFramework;
import cdr.joglFramework.renderer.OpaqueRendererWithGUI;
import evaluations.EvaluationField;
import javafx.beans.property.SimpleIntegerProperty;
import models.visibilityInteriorsModel.VisibilityInteriorsModelBuilder;
import models.visibilityInteriorsModel.VisibilityInteriorsModelEvaluator;
import models.visibilityInteriorsModel.VisibilityInteriorsModelExporter;
import models.visibilityInteriorsModel.VisibilityInteriorsModelRenderer;
import models.visibilityInteriorsModel.VisibilityInteriorsModelEvaluator.EvaluationType;
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
	private VisibilityInteriorsModelExporter exporter = new VisibilityInteriorsModelExporter(); // TODO - make setters
	
	private GLCameraAxonometric cameraAxon;
	private GLCameraPlan cameraPlan;
	
	private EvaluationField fieldEvaluation;
	
	private SimpleIntegerProperty evaluationLabelIndex = new SimpleIntegerProperty(0);
	
	private String[] evalutatioLabels = new String[] {
			"visibility",
			"accessibility",
			"discoverability",
			"catchment",
			"perception",
			"vantage",
			"distance"
	};
	
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
	
	public VisibilityInteriorsModelExporter getExporter() {
		return exporter;
	}
	
	public EvaluationField getFieldEvaluation() {
		return fieldEvaluation;
	}
	
	public SimpleIntegerProperty getEvalutationLabelIndex() {
		return evaluationLabelIndex;
	}
	
	public String getEvaluationLabel() {
		return evalutatioLabels[evaluationLabelIndex.get()];
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
		
		framework.getKeyListeners().add(new SimpleKeyListener() {
			
			public void keyTyped(KeyEvent e) {			
				
				if(e.getKeyChar() == 'd') {
					
					if (evaluationLabelIndex.get() == evalutatioLabels.length-1) evaluationLabelIndex.set(0);
					else evaluationLabelIndex.set(evaluationLabelIndex.get() + 1);	
				
					renderer.setEvaluationLabel(evalutatioLabels[evaluationLabelIndex.get()]);
					
					System.out.println(evalutatioLabels[evaluationLabelIndex.get()]);
				}
				
				if(e.getKeyChar() == 'a') {
					
					if (evaluationLabelIndex.get() == 0) evaluationLabelIndex.set(evalutatioLabels.length-1);
					else evaluationLabelIndex.set(evaluationLabelIndex.get() - 1);	
				
					renderer.setEvaluationLabel(evalutatioLabels[evaluationLabelIndex.get()]);
					
					System.out.println(evalutatioLabels[evaluationLabelIndex.get()]);
				}

				
				e.consume();
				
				
			}
			
		});
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
	
	public void evaluate() {
		
		renderer.setEvaluationField(null);
		
		fieldEvaluation = (EvaluationField) evaluator.getEvaluation(EvaluationType.VISIBILITY);
						
		if (fieldEvaluation != null && model != null) {	
										
			fieldEvaluation.setResolution(model.getResolution());
			evaluator.evaluateModel(model, fieldEvaluation);					
			renderer.setEvaluationField(fieldEvaluation);		
		}
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
