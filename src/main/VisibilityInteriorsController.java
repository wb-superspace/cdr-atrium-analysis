package main;

import java.io.File;
import java.net.URL;
import java.time.chrono.ThaiBuddhistEra;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import cdr.colour.HSVColour;
import cdr.fileIO.dxf2.DXFDocument2;
import cdr.geometry.primitives.Point3D;
import evaluations.EvaluationField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.legend.LegendItem;
import javafx.legend.VBoxLegend;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import models.visibilityInteriorsModel.VisibilityInteriorsModelEvaluator.EvaluationType;
import javafx.stage.FileChooser.ExtensionFilter;

public class VisibilityInteriorsController implements Initializable{

	VisibilityInteriorsApplication application;
	
	public MenuItem importGeometryMenuItem;
	public MenuItem evaluateModelMenuItem;
	
	public TitledPane legendTitledPane;
	public VBox legendVBox;
	
	public VisibilityInteriorsController(VisibilityInteriorsApplication application) {
		
		this.application = application;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		initializeRunMenu();
		initializeImportMenu();
		initializeLegend();
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

				if (file != null) {
					
					application.setDXF(new DXFDocument2(file));
					
					application.clear();
					application.build();
									
					application.getRenderer().setEvaluationField(null);
					application.getRenderer().setEvaluationLabel(null);
				}
							
				if (application.getModel() != null) {

				}
			}
		});
	}
	
	public void initializeRunMenu() {
						
		evaluateModelMenuItem.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				
				application.evaluate();
				
				updateLegend();
			}		
		});
	}
	
	public void initializeLegend() {
		
		this.application.getEvalutationLabelIndex().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
								
				updateLegend();
			}
		});
	}
	
	public void updateLegend() {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {

				if (application.getModel() != null && application.getFieldEvaluation() != null) {
					
					legendVBox.getChildren().clear();
					
					String evaluationLabel = application.getEvaluationLabel();
					
					EvaluationField ef = application.getFieldEvaluation();
					
					legendTitledPane.setText(evaluationLabel);
					
					HashMap<Float, HashMap<Point3D, Float>> results = ef.getValues(evaluationLabel);
					float[] domain = ef.getValueDomain(evaluationLabel);
									
					if (results == null) {					
						return;
					}
						
					Map<Float, String> legend = ef.getValueLegend(evaluationLabel, application.getModel().getLayouts().keySet(), false);
					
					ObservableList<LegendItem> legendItems = FXCollections.observableArrayList();
					
					for (Map.Entry<Float, String> valueEntry : legend.entrySet()) {
						
						HSVColour c = new HSVColour() ;
						c.setHSV((1 - valueEntry.getKey()) * 0.6f, 1f, 1f) ;
						
						legendItems.add(new LegendItem(null, valueEntry.getValue(), new float[] {c.red(), c.green(), c.blue()}));
					}
					
					legendVBox.getChildren().add(new VBoxLegend<>(legendItems, 150, 3));
				}
			}
		});
	}
}
