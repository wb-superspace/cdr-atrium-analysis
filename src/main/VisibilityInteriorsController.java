package main;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import cdr.colour.HSVColour;
import cdr.fileIO.dxf2.DXFDocument2;
import evaluations.VisibilityInteriorsEvaluation.EvaluationType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.legend.BarChartItem;
import javafx.legend.CheckboxLegendItem;
import javafx.legend.GridPaneBarChart;
import javafx.legend.VBoxLegend;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import math.ValueMapper;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation.LocationType;

public class VisibilityInteriorsController implements Initializable{

	VisibilityInteriorsApplication application;
	
	public MenuItem importGeometryMenuItem;
	public MenuItem evaluateModelMenuItem;
	
	public TitledPane legendTitledPane;
	public VBox legendVBox;
	
	public TitledPane settingsTitledPane;
	public VBox settingsVBox;
	
	public VBox evaluationVBox;
	public VBox fromVBox;
	public VBox toVBox;
	
	public VBox filterVBox;
	public VBox typeVBox;
		
	public Map<CheckboxLegendItem, LocationType> fromTypes = new HashMap<>();
	public Map<CheckboxLegendItem, LocationType> toTypes = new HashMap<>();
	public Map<CheckboxLegendItem, EvaluationType> evaluationTypes = new HashMap<>();
	
	public AnchorPane controlsPane;
	
	public boolean sliderChanged = false;
	
	public VisibilityInteriorsController(VisibilityInteriorsApplication application) {
		
		this.application = application;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		initializeRunMenu();
		initializeImportMenu();
		initializeSettings();
		initializeEvaluations();
		initializeLegend();
		initializeControls();
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
					
					if (application.getModel() != null) {
						initializeLegend();
					}
					
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
				
				List<LocationType> sinks = new ArrayList<>();
				List<LocationType> sources = new ArrayList<>();
				
				EvaluationType type = null;
				
				for (Map.Entry<CheckboxLegendItem, LocationType> fromItem : fromTypes.entrySet()) {
					if (fromItem.getKey().getFlag().getValue() == true) {
						sources.add(fromItem.getValue());
					}
				}
				
				for (Map.Entry<CheckboxLegendItem, LocationType> toItem : toTypes.entrySet()) {
					if (toItem.getKey().getFlag().getValue() == true) {
						sinks.add(toItem.getValue());
					}
				}
				
				for (Map.Entry<CheckboxLegendItem, EvaluationType> typeItem : evaluationTypes.entrySet()) {
					if (typeItem.getKey().getFlag().getValue() == true) {
						type = typeItem.getValue();
					}
				}
				
				if (type == null) {
					return;
				}
				
				application.evaluate(sinks, sources, type);
				
				updateLegend();
			}		
		});
	}
	
	public void initializeControls() {
		
		application.controls.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {				
				controlsPane.setVisible(newValue);
			}
		});
	}
	
	public void initializeEvaluations() {
		
		ObservableList<CheckboxLegendItem> fromItems = FXCollections.observableArrayList();
		ObservableList<CheckboxLegendItem> toItems = FXCollections.observableArrayList();
		ObservableList<CheckboxLegendItem> filterItems = FXCollections.observableArrayList();
		ObservableList<CheckboxLegendItem> typeItems = FXCollections.observableArrayList();
		
		for (LocationType type : LocationType.values()) {
			
			CheckboxLegendItem fromItem = new CheckboxLegendItem(null, type.toString().toLowerCase(), new float[] {0.5f,0.5f,0.5f});
			CheckboxLegendItem toItem = new CheckboxLegendItem(null, type.toString().toLowerCase(), new float[] {0.5f,0.5f,0.5f});
			
			fromItems.add(fromItem);
			toItems.add(toItem);
			
			fromTypes.put(fromItem, type);
			toTypes.put(toItem, type);
		}
		
		fromVBox.getChildren().add(new VBoxLegend<>(fromItems, 125, 3));
		toVBox.getChildren().add(new VBoxLegend<>(toItems, 125, 3));
		
		CheckboxLegendItem onlyVisibleItem = new CheckboxLegendItem(null, "visible", new float[] {0.5f,0.5f,0.5f});
		onlyVisibleItem.getFlag().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				application.visibleFilter = newValue;
			}
		});
		filterItems.add(onlyVisibleItem);
		
		CheckboxLegendItem onlySingleFloor = new CheckboxLegendItem(null, "single floor", new float[] {0.5f,0.5f,0.5f});
		onlySingleFloor.getFlag().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				application.singleFloorFilter = newValue;
			}
		});
		filterItems.add(onlySingleFloor);
		
		CheckboxLegendItem onlySingleSink = new CheckboxLegendItem(null, "closest sink", new float[] {0.5f,0.5f,0.5f});
		onlySingleSink.getFlag().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				application.multipleSinkFilter = newValue;
			}
		});
		filterItems.add(onlySingleSink);
		
		filterVBox.getChildren().add(new VBoxLegend<>(filterItems, 250, 3));	
		
		for (EvaluationType type : EvaluationType.values()) {
			
			CheckboxLegendItem typeItem = new CheckboxLegendItem(null, type.toString().toLowerCase(), new float[] {0.5f,0.5f,0.5f});
			
			typeItems.add(typeItem);
			
			evaluationTypes.put(typeItem, type);
						
			typeItem.getFlag().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					
					if (newValue) {
						
						for (CheckboxLegendItem otherItem : evaluationTypes.keySet()) {
							if (!typeItem.equals(otherItem)) {
								otherItem.getFlag().setValue(false);
							}
						}
						
						if (type == EvaluationType.EXPOSURE || type == EvaluationType.VISIBILITY) {
							
							onlySingleSink.getFlag().set(false);
							onlySingleFloor.getFlag().set(false);
							onlyVisibleItem.getFlag().set(true);
							
							filterVBox.setVisible(false);
						} else {
							filterVBox.setVisible(true);
						}
						
						if (type == EvaluationType.VISIBILITY) {
							
							fromVBox.setVisible(false);		
						} else {						
							fromVBox.setVisible(true);
						}
					}
				}
			});
		}
		
		typeVBox.getChildren().add(new VBoxLegend<>(typeItems, 125, 3));
	}
		
	public void initializeSettings() {
		
		Slider distanceSlider = new Slider();
		distanceSlider.setValue(0);
		distanceSlider.setMin(0);
		distanceSlider.setMax(50);
		distanceSlider.setBlockIncrement(15);
		distanceSlider.setShowTickLabels(true);
		distanceSlider.setShowTickMarks(true);
		distanceSlider.setMajorTickUnit(10);
		distanceSlider.setMinorTickCount(1);
		distanceSlider.setPrefWidth(250);
		
		Label distanceLabel = new Label("visibility distance (m) : none");
		
		Slider lengthSlider = new Slider();
		lengthSlider.setValue(0);
		lengthSlider.setMin(0);
		lengthSlider.setMax(500);
		lengthSlider.setBlockIncrement(150);
		lengthSlider.setShowTickLabels(true);
		lengthSlider.setShowTickMarks(true);
		lengthSlider.setMajorTickUnit(100);
		lengthSlider.setMinorTickCount(1);
		lengthSlider.setPrefWidth(250);
		
		Label lengthLabel = new Label("walking distance (m) : none");
				
		distanceSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				 if (! newValue) {
					 
					 if (application.evaluation != null) {
					 
					 	float value = (int) distanceSlider.getValue();
					 
						String label = null;
						
						if (value == Float.MAX_VALUE || value == 0) {
							label = "none";
						} else {
							label = Float.toString(value);
						}
					 	
						distanceLabel.setText("visibility distance (m) : " + label);
					 	
					 	distanceSlider.setValue(value);
					 	
						new Thread(new Runnable() {
						    public void run() {
						    	
						    	sliderChanged = true;
						    	
						    	distanceSlider.setDisable(true);
						    	lengthSlider.setDisable(true);
						    						
								application.getModel().getLocationsActive().forEach(l -> {
									
									l.getEvaluation().setMaxDistance(value);
									l.getEvaluation().evaluate();
									
								});
								
								application.setEvaluation();
								
								distanceSlider.setDisable(false);
								lengthSlider.setDisable(false);
								
								sliderChanged = false;
								 	
						    }
						}).start();
				 	}

				 }

			}
		});
		
		settingsVBox.getChildren().add(distanceLabel);
		settingsVBox.getChildren().add(distanceSlider);		
		application.update.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {				
				
				if (application.evaluation != null) {
					
					if (!sliderChanged) {
						
						float value = application.evaluation.getMaxDistance();
												
						distanceSlider.setValue(value);
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								
								String label = null;
								
								if (value == Float.MAX_VALUE || value == 0) {
									label = "none";
								} else {
									label = Float.toString(value);
								}
								
								distanceLabel.setText("visibility distance (m) : " + label);
							}
						});
						
					}
				}
			}
		});
						
		lengthSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				 if (! newValue) {
					 
					 if (application.evaluation != null) {
					 
					 	float value = (int) lengthSlider.getValue();
					 
						String label = null;
						
						if (value == Float.MAX_VALUE || value == 0) {
							label = "none";
						} else {
							label = Float.toString(value);
						}
					 	
						lengthLabel.setText("walking distance (m) : " + label);
					 	
						lengthSlider.setValue(value);
					 	
						new Thread(new Runnable() {
						    public void run() {
						    	
						    	sliderChanged = true;
						    	
						    	lengthSlider.setDisable(true);
						    	distanceSlider.setDisable(true);
						    						
								application.getModel().getLocationsActive().forEach(l -> {
									
									l.getEvaluation().setMaxLength(value);
									l.getEvaluation().evaluate();
									
								});
								
								application.setEvaluation();
								
								lengthSlider.setDisable(false);
								distanceSlider.setDisable(false);
								
								sliderChanged = false;
								 	
						    }
						}).start();
				 	}

				 }

			}
		});
		
		settingsVBox.getChildren().add(lengthLabel);
		settingsVBox.getChildren().add(lengthSlider);		
		application.update.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {				
				
				if (application.evaluation != null) {
					
					if (!sliderChanged) {
						
						float value = application.evaluation.getMaxLength();
												
						lengthSlider.setValue(value);
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								
								String label = null;
								
								if (value == Float.MAX_VALUE || value == 0) {
									label = "none";
								} else {
									label = Float.toString(value);
								}
								
								lengthLabel.setText("walking distance (m) : " + label);
							}
						});
						
					}
				}
			}
		});

	}
	
	public void initializeLegend() {
		
		application.update.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {				
				updateLegend();
			}
		});
	}
		
	public void updateLegend() {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				
				legendTitledPane.setText(application.label);
				legendVBox.getChildren().clear();

				if (application.evaluation == null) {
					return;
				}
				
				SortedMap<Float, Integer> bins = application.evaluation.getBinnedSinkValues();
				ObservableList<BarChartItem> legendItems = FXCollections.observableArrayList();
				
				float min = bins.firstKey();
				float max = bins.lastKey();
				float avg = 0f;
				float count = 0f;
				
				for (VisibilityInteriorsLocation sink : application.evaluation.getSinks()) {
					avg += application.evaluation.getSinkValue(sink);
					count ++;
				}
				
				for (Map.Entry<Float, Integer> bin : bins.entrySet()) {
					
					float colorValue = ValueMapper.map(bin.getKey(), min, max, 0, 1);
								
					float binValue = bin.getKey();
					
					String binLabel = null;
					
					if (binValue > 0 && binValue < 1) {
						binLabel = String.format("%.2f", binValue * 100);
					} else {
						binLabel = String.format("%.2f", binValue);
					}
					
					int binCount = bin.getValue();
					float countPercentage = (float) binCount / (float) application.evaluation.getSinks().size();
					
					HSVColour colour = new HSVColour();
					colour.setHSV((1-colorValue) * 0.6f, 1f, 1f) ;	
					
					float[] colorF = new float[] {colour.red(), colour.green(), colour.blue()};
					
					BarChartItem item = new BarChartItem(binLabel, Integer.toString(binCount), colorF, countPercentage, 150f, 10f);
					
					item.getBeforeBarLabel().setPrefWidth(50);
					item.getAfterBarLabel().setPrefWidth(50);
					
					legendItems.add(item);
				}
				
				String avgLabel = null;
				
				if (min >=0 && max <=1) {
					avgLabel = "average : " + String.format("%.2f", avg / count * 100) + "%";
				} else {
					avgLabel = "average : " + avg / count;
				}
								
				legendVBox.getChildren().add(new GridPaneBarChart<>(legendItems));
				legendVBox.getChildren().add(new Label(avgLabel));
			}
		});
	}
}
