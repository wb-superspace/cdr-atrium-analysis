package main;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import cdr.fileIO.dxf2.DXFDocument2;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
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
		
		// TODO
	}
	
	public void updateLegend() {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {

				// TODO
			}
		});
	}
}
