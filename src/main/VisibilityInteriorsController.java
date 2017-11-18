package main;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import cdr.colour.Colour;
import cdr.colour.HSVColour;
import cdr.fileIO.dxf2.DXFDocument2;
import color.VisibilityInteriorsColourMaps;
import evaluations.VisibilityInteriorsEvaluation.EvaluationType;
import evaluations.VisibilityInteriorsEvaluation.ValueType;
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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import math.ValueMapper;
import model.location.VisibilityInteriorsLocation;
import model.location.VisibilityInteriorsLocation.LocationType;

public class VisibilityInteriorsController implements Initializable{

	VisibilityInteriorsApplication application;
	
	public MenuItem importGeometryMenuItem;
	public MenuItem evaluateModelMenuItem;
	public MenuItem cameraSettingsMenuItem;
	
	public TitledPane legendTitledPane;
	public VBox sinkLegendVBox;
	public VBox sourceLegendVBox;
	
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
		initializeCameraSettings();
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
	
	public void initializeCameraSettings() {
		
		cameraSettingsMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				if (application.cameraOblique != null) {
					
					Alert alert = new Alert(AlertType.NONE);
					
					alert.setWidth(400);
					
					DialogPane pane = new DialogPane();
					
					GridPane gridPane = new GridPane();
					
					Label cameraXLabel = new Label(" camera x : ");
					cameraXLabel.setMinWidth(100);
					Slider cameraXSlider = new Slider();
					cameraXSlider.setMinWidth(250);
					cameraXSlider.setMin(0);
					cameraXSlider.setMax(30);
					cameraXSlider.setValue(application.cameraXZ);
					
					cameraXSlider.valueProperty().addListener(new ChangeListener<Number>() {

						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue,
								Number newValue) {
							
							application.cameraXZ = newValue.floatValue();
							application.cameraOblique.setXZOffsetRatio(application.cameraXZ);
						}
					});
					
					
					Label cameraYLabel = new Label(" camera y : ");
					cameraYLabel.setMinWidth(100);
					Slider cameraYSlider = new Slider();
					cameraYSlider.setMinWidth(250);
					cameraYSlider.setMin(0);
					cameraYSlider.setMax(30);
					cameraYSlider.setValue(application.cameraYZ);
					
					cameraYSlider.valueProperty().addListener(new ChangeListener<Number>() {

						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue,
								Number newValue) {
							
							application.cameraYZ = newValue.floatValue();
							application.cameraOblique.setYZOffsetRatio(application.cameraYZ);
						}
					});
					
					
					gridPane.add(cameraXLabel, 0, 0);
					gridPane.add(cameraXSlider, 1, 0);
					
					gridPane.add(cameraYLabel, 0, 1);
					gridPane.add(cameraYSlider, 1, 1);
													
					alert.setDialogPane(pane);
					alert.getDialogPane().getChildren().add(gridPane);
					alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
					alert.showAndWait();
					
				}
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
		distanceSlider.setMax(500);
		distanceSlider.setBlockIncrement(150);
		distanceSlider.setShowTickLabels(true);
		distanceSlider.setShowTickMarks(true);
		distanceSlider.setMajorTickUnit(100);
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
		
		Function<Object, Object> distanceFunction = new Function<Object, Object>() {

			@Override
			public Object apply(Object arg0) {
				
				 if (application.evaluation != null) {
					 
					 	float value = (int) distanceSlider.getValue();
					 
						String label = null;
						
						if (value == Float.MAX_VALUE || value == 0) {
							label = "none";
						} else {
							label = Float.toString(value);
						}
						
						final String fLabel = label;
					 	
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								distanceLabel.setText("visibility distance (m) : " + fLabel);
							}
						});
						
					 	distanceSlider.setValue(value);
					 	
						new Thread(new Runnable() {
						    public void run() {
						    	
						    	sliderChanged = true;
						    	
						    	distanceSlider.setDisable(true);
						    	lengthSlider.setDisable(true);
						    	
						    	application.setVisibilityCatchment(value);								
								application.setEvaluation();
								
								distanceSlider.setDisable(false);
								lengthSlider.setDisable(false);
								
								sliderChanged = false;
								 	
						    }
						}).start();
				 	}
				
				return null;
			}
		};
				
		distanceSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				 if (! newValue) {			 
					 distanceFunction.apply(null);
				 }
			}
		});
		
		distanceSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				if (!distanceSlider.isValueChanging()) {					
					distanceFunction.apply(null);
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
		
		Function<Object, Object> lengthFunction = new Function<Object, Object>() {

			@Override
			public Object apply(Object t) {

				 if (application.evaluation != null) {
					 
					 	float value = (int) lengthSlider.getValue();
					 
						String label = null;
						
						if (value == Float.MAX_VALUE || value == 0) {
							label = "none";
						} else {
							label = Float.toString(value);
						}
						
						final String fLabel = label;
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								lengthLabel.setText("walking distance (m) : " + fLabel);
							}
						});
					 	
						lengthSlider.setValue(value);
					 	
						new Thread(new Runnable() {
						    public void run() {
						    	
						    	sliderChanged = true;
						    	
						    	lengthSlider.setDisable(true);
						    	distanceSlider.setDisable(true);
						    						
						    	application.setDistanceCatchment(value);
								application.setEvaluation();
								
								lengthSlider.setDisable(false);
								distanceSlider.setDisable(false);
								
								sliderChanged = false;
								 	
						    }
						}).start();
				 	}
				
				return null;
			}
		};
						
		lengthSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				 if (! newValue) {	 
					 lengthFunction.apply(null);
				 }
			}
		});
		
		lengthSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				if (!lengthSlider.isValueChanging()) {					
					lengthFunction.apply(null);
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
				
				updateSinkValues();
				updateSourceValues();
			}
		});
	}
	
	private void updateSinkValues() {
		
		legendTitledPane.setText(application.label);
		sinkLegendVBox.getChildren().clear();

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
			
			if (application.evaluation.getValueType() == ValueType.PERCENTAGE) {
				binLabel = String.format("%.2f", binValue * 100);
			} else {
				binLabel = String.format("%.0f", binValue);
			}
			
			int binCount = bin.getValue();
			float countPercentage = (float) binCount / (float) application.evaluation.getSinks().size();
			
			Colour colour = VisibilityInteriorsColourMaps.getSinkColourMap(application.evaluation).get(colorValue);
			
			float[] colorF = new float[] {colour.red(), colour.green(), colour.blue()};
			
			BarChartItem item = new BarChartItem(binLabel, Integer.toString(binCount), colorF, countPercentage, 75f, 10f);
			
			item.getBeforeBarLabel().setPrefWidth(50);
			item.getAfterBarLabel().setPrefWidth(50);
			
			legendItems.add(item);
		}
		
		if (application.evaluation.isValueReversed()) FXCollections.reverse(legendItems);
		
		String avgLabel = null;
		
		if (application.evaluation.getValueType() == ValueType.PERCENTAGE) {
			avgLabel = "average : " + String.format("%.2f", avg / count * 100) + "%";
		} else {
			avgLabel = "average : " + avg / count;
		}
						
		sinkLegendVBox.getChildren().add(new GridPaneBarChart<>(legendItems));
		sinkLegendVBox.getChildren().add(new Label(avgLabel));
	}
	
	private void updateSourceValues() {
		
		legendTitledPane.setText(application.label);
		sourceLegendVBox.getChildren().clear();

		if (application.evaluation == null) {
			return;
		}
		
		SortedMap<Float, Integer> bins = application.evaluation.getBinnedSourceValues();
		ObservableList<BarChartItem> legendItems = FXCollections.observableArrayList();
		
		float min = bins.firstKey();
		float max = bins.lastKey();
		float avg = 0f;
		float count = 0f;
		
		for (VisibilityInteriorsLocation source : application.evaluation.getSources()) {
			avg += application.evaluation.getSourceValue(source);
			count ++;
		}
		
		for (Map.Entry<Float, Integer> bin : bins.entrySet()) {
			
			float colorValue = ValueMapper.map(bin.getKey(), min, max, 0, 1);
						
			float binValue = bin.getKey();
			
			String binLabel = null;
			
			if (application.evaluation.getValueType() == ValueType.PERCENTAGE) {
				binLabel = String.format("%.2f", binValue * 100);
			} else {
				binLabel = String.format("%.0f", binValue);
			}
			
			int binCount = bin.getValue();
			float countPercentage = (float) binCount / (float) application.evaluation.getSources().size();
			
			Colour colour = VisibilityInteriorsColourMaps.getSourceColourMap(application.evaluation).get(colorValue);
			
			float[] colorF = new float[] {colour.red(), colour.green(), colour.blue()};
			
			BarChartItem item = new BarChartItem(binLabel, Integer.toString(binCount), colorF, countPercentage, 75f, 10f);
			
			item.getBeforeBarLabel().setPrefWidth(50);
			item.getAfterBarLabel().setPrefWidth(50);
			
			legendItems.add(item);
		}
		
		if (application.evaluation.isValueReversed()) FXCollections.reverse(legendItems);
		
		String avgLabel = null;
		
		if (application.evaluation.getValueType() == ValueType.PERCENTAGE) {
			avgLabel = "average : " + String.format("%.2f", avg / count * 100) + "%";
		} else {
			avgLabel = "average : " + avg / count;
		}
						
		sourceLegendVBox.getChildren().add(new GridPaneBarChart<>(legendItems));
		sourceLegendVBox.getChildren().add(new Label(avgLabel));
	}
}
