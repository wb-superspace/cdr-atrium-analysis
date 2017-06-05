package main;

import java.io.IOException;

import cdr.gui.javaFX.JavaFXGUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;


public class VisibilityInteriorsMain extends JavaFXGUI<VisibilityInteriorsApplication>{

	VisibilityInteriorsApplication application;
	String styleSheetPath; 
	
	public VisibilityInteriorsMain(VisibilityInteriorsApplication application) {
		super(application);
		
		this.application = application;
	}
	
	public static void main(String[] args) {
		
		VisibilityInteriorsMain gui = new VisibilityInteriorsMain(new VisibilityInteriorsApplication());
		gui.setStyleSheetPath("main/GUIStyleSheet.css");
		gui.buildAndShowGUI("Visibility Interiors");
	}
	
	@Override
	public String getStyleSheetPath() {
		
		return this.styleSheetPath;
	}

	public void setStyleSheetPath(String styleSheetPath) {
		this.styleSheetPath = styleSheetPath;
	}
	
	@Override
	protected Pane createPane(VisibilityInteriorsApplication application) {
		
		Pane pane = null;
		
		try {
			FXMLLoader floader = new FXMLLoader(VisibilityInteriorsMain.class.getResource("GUITemplate.fxml"));
			floader.setController(new VisibilityInteriorsController(application));
			pane = (Pane)floader.load();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return pane;
	}
}
