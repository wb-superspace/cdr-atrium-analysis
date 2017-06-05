package main;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import cdr.fileIO.dxf2.DXFDocument2;
import evaluations.EvaluationField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import models.visibilityInteriorsModel.VisibilityInteriorsModelEvaluator.EvaluationType;
import javafx.stage.FileChooser.ExtensionFilter;

public class VisibilityInteriorsController implements Initializable{

	VisibilityInteriorsApplication application;
	
	public MenuItem importGeometryMenuItem;
	public MenuItem evaluateModelMenuItem;
	
	EvaluationField fieldEvaluation;
	
	public VisibilityInteriorsController(VisibilityInteriorsApplication application) {
		
		this.application = application;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		initializeRunMenu();
		initializeImportMenu();
	}
	
	public void initializeImportMenu() {
		
		importGeometryMenuItem.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				FileChooser fc = new FileChooser();
				ExtensionFilter dxfExtensionFilter = new ExtensionFilter("DXF", "*.dxf") ; 
				fc.getExtensionFilters().add(dxfExtensionFilter); 
				fc.setInitialDirectory(new File(System.getProperty("user.dir")));
				fc.setSelectedExtensionFilter(dxfExtensionFilter);
				File file = fc.showOpenDialog(null);

				application.setDXF(new DXFDocument2(file));
				
				application.clear();
				application.build();
								
				application.getRenderer().setEvaluationField(null);
				application.getRenderer().setEvaluationLabel(null);
				
				if (application.getModel() != null) {

				}
			}
		});
	}
	
	public void initializeRunMenu() {
						
		evaluateModelMenuItem.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				
				application.getRenderer().setEvaluationField(null);
				
				fieldEvaluation = (EvaluationField) application.getEvaluator().getEvaluation("Visibility");
								
				if (fieldEvaluation != null &&  application.getModel() != null) {	
												
					fieldEvaluation.setResolution(application.getModel().getResolution());
					application.getEvaluator().evaluateModel(application.getModel(), fieldEvaluation);					
					application.getRenderer().setEvaluationField(fieldEvaluation);		
				}
			}		
		});
	}
}
