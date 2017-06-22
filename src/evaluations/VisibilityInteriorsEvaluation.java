package evaluations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.accessibility.internal.resources.accessibility;

import cdr.geometry.primitives.LineSegment3D;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluation {
	
	private String label = null;
	
	private boolean onlyVisible;
	private boolean onlyModifiable;
	
	/*
	 * output
	 */
	
	private Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, VisibilityInteriorsPath>> paths = new HashMap<>();
	
	private Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, Float>> sourceValues = new HashMap<>();
	
	private Map<VisibilityInteriorsLocation, Float> sinkValues = new HashMap<>();
	
	/*
	 * render
	 */
	
	private Map<LineSegment3D, Float> edgeCounts = new ConcurrentHashMap<>();
	
	private Map<LineSegment3D, Float> edgeValues = new ConcurrentHashMap<>();
	
	/*
	 * eval
	 */
		
	public VisibilityInteriorsEvaluation(String label, boolean onlyVisible, boolean onlyModifiable) {
		this.label = label;
		this.onlyVisible = onlyVisible;
		this.onlyModifiable = onlyModifiable;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public Collection<VisibilityInteriorsLocation> getSinks() {
		return sinkValues.keySet();
	}
	
	public float getSinkValue (VisibilityInteriorsLocation location) {
		
		if (this.sinkValues.containsKey(location)) {
			return this.sinkValues.get(location);
		}
		
		return 0f;
		
	}
	
	public Map<VisibilityInteriorsLocation, Float> getSinkValues() {
		return this.sinkValues;
	}
	
	public Collection<VisibilityInteriorsLocation> getSources(VisibilityInteriorsLocation sink) {
		return this.sourceValues.get(sink).keySet();
	}
	
	public float getSourceValue (VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source) {
		return this.sourceValues.get(sink).get(source);
	}
	
	public Map<VisibilityInteriorsLocation, Float> getSourceValues(VisibilityInteriorsLocation sink) {
		return this.sourceValues.get(sink);
	}
		
	public VisibilityInteriorsPath getPath(VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source) {
		return this.paths.get(sink).get(source);
	}
	
	public Collection<LineSegment3D> getEdges() {
		return this.edgeCounts.keySet();
	}
	
	public float getEdgeCount (LineSegment3D connection) {
		
		if (this.edgeCounts.containsKey(connection)) {
			return this.edgeCounts.get(connection);
		}
		
		return 0f;
	}
	
	public float getEdgeValue (LineSegment3D connection) {
		
		if (this.edgeValues.containsKey(connection)) {
			return this.edgeValues.get(connection);
		}
		
		return 0f;
	}
			
	public void	evaluate(List<VisibilityInteriorsLocation> locations) {
		
		List<VisibilityInteriorsLocation> sources = new ArrayList<>();
		
		edgeCounts.clear();
		edgeValues.clear();
		
		paths = new HashMap<>();
		sourceValues = new HashMap<>();
		sinkValues = new HashMap<>();
	
		for (VisibilityInteriorsLocation location : locations) {
			
			paths.put(location, new HashMap<>());
			sourceValues.put(location, new HashMap<>());
			
			for (VisibilityInteriorsLocation target : location.getVisibilityPathLocations()) {
				if (!onlyModifiable || target.isModifiable()) {
					if (!onlyVisible || location.getVisibilityPath(target).getLocations().size() == 2) {
						if (!sources.contains(target) && !locations.contains(target)) {
							sources.add(target);
						}
					}
				}
			}
		}
						
		for (VisibilityInteriorsLocation source : sources) {
			
			VisibilityInteriorsPath minPath = null;
			VisibilityInteriorsLocation minSink = null;
			
			for (VisibilityInteriorsLocation location : locations) {
				
				VisibilityInteriorsPath path = location.getConnectivityPath(source);
				
				if (path == null) {					
					continue;
				
				} else if (minPath == null || path.getLength() < minPath.getLength()) {
					minPath = path;
					minSink = location;
				}
			}
						
			if (minPath == null) {							
				continue; 
			}
			
			paths.get(minSink).put(source, minPath);
			sourceValues.get(minSink).put(source, minPath.getAccessibility());
			
			for (VisibilityInteriorsConnection connection : minPath.getConnections()) {
				
				if (!edgeCounts.containsKey(connection.getGeometry())) {
					edgeCounts.put(connection.getGeometry(), 0f);
				}
				
				VisibilityInteriorsPath connectionPath = minSink.getConnectivityPath(connection.getStartLocation());
				
				edgeCounts.put(connection.getGeometry(), edgeCounts.get(connection.getGeometry()) + 1);
				edgeValues.put(connection.getGeometry(), connectionPath.getAccessibility());
			}
		}
		
		for (VisibilityInteriorsLocation location : locations) {
			
			float average = 0f;
			
			for (float value : sourceValues.get(location).values()) {
				average += value / (float) sourceValues.get(location).size();
			}
			
			sinkValues.put(location, average);
		}
	}
		
	public static VisibilityInteriorsEvaluation mergeEvaluations(String label, List<VisibilityInteriorsEvaluation> evaluations) {
		
		VisibilityInteriorsEvaluation merged = new VisibilityInteriorsEvaluation(label, false, false);
		
		Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, VisibilityInteriorsPath>> paths = new HashMap<>();
		
		Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, Float>> sourceValues = new HashMap<>();
		
		Map<VisibilityInteriorsLocation, List<Float>> sinkValues = new HashMap<>();
		
		Map<LineSegment3D, List<Float>> edgeCounts = new ConcurrentHashMap<>();
		
		Map<LineSegment3D, List<Float>> edgeValues = new ConcurrentHashMap<>();
		
		for (VisibilityInteriorsEvaluation evaluation : evaluations) {
			paths.putAll(evaluation.paths);
			
			for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {
				
				if (!sourceValues.containsKey(sink)) {
					sourceValues.put(sink, new HashMap<>());
				}
				
				sourceValues.get(sink).putAll(evaluation.getSourceValues(sink));
				
				if (!sinkValues.containsKey(sink)) {
					sinkValues.put(sink, new ArrayList<>());
				}
				
				sinkValues.get(sink).add(evaluation.getSinkValue(sink));
			}
			
			for (LineSegment3D edge : evaluation.getEdges()) {
				
				LineSegment3D rev = new LineSegment3D(edge.getEndPoint(), edge.getStartPoint());
				
				if (edgeCounts.containsKey(edge)) {
					
					edgeCounts.get(edge).add(evaluation.getEdgeCount(edge));
					
				} else if (edgeCounts.containsKey(rev)) {
					
					edgeCounts.get(rev).add(evaluation.getEdgeCount(edge));
					
				} else {
					
					edgeCounts.put(edge, new ArrayList<>());
					edgeCounts.get(edge).add(evaluation.getEdgeCount(edge));
				}

				
				if (edgeValues.containsKey(edge)) {
					
					edgeValues.get(edge).add(evaluation.getEdgeValue(edge));
					
				} else if (edgeValues.containsKey(rev)) {
					
					edgeValues.get(rev).add(evaluation.getEdgeValue(edge));
					
				} else {
					
					edgeValues.put(edge, new ArrayList<>());
					edgeValues.get(edge).add(evaluation.getEdgeValue(edge));
				}
			}
		}
		
		merged.paths = paths;
		merged.sourceValues = sourceValues;
		
		for (Map.Entry<VisibilityInteriorsLocation, List<Float>> sinkValue : sinkValues.entrySet()) {
			
			float avg = 0f;
			
			for (Float val : sinkValue.getValue()) {
				avg += val / (float) sinkValue.getValue().size();
			}
			
			merged.sinkValues.put(sinkValue.getKey(), avg);
		}
		
		for (Map.Entry<LineSegment3D, List<Float>> edgeCount : edgeCounts.entrySet()) {
			
			float sum = 0f;
			
			for (Float val : edgeCount.getValue()) {
				sum += val;
			}
			
			merged.edgeCounts.put(edgeCount.getKey(), sum);
		}

		for (Map.Entry<LineSegment3D, List<Float>> edgeValue : edgeValues.entrySet()) {
			
			float avg = 0f;
			
			for (Float val : edgeValue.getValue()) {
				avg += val; 
				//avg += val / (float) edgeValue.getValue().size();
			}
			
			merged.edgeValues.put(edgeValue.getKey(), avg);
		}

		return merged;
	}
}
