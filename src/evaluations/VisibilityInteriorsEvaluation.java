package evaluations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.accessibility.internal.resources.accessibility;

import cdr.colour.HSVColour;
import cdr.geometry.primitives.LineSegment3D;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.legend.LegendItem;
import math.ValueMapper;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;

public class VisibilityInteriorsEvaluation {
	
	private String label = null;
	
	protected boolean onlyVisible;
	protected boolean onlyModifiable;
	protected boolean onlySingleFloor;
	protected boolean onlyAccess;
	
	protected boolean isCumulator = false;
	
	protected float minValue = Float.MAX_VALUE;
	protected float maxValue = -Float.MAX_VALUE;
	
	protected float maxDistance = Float.MAX_VALUE;
	protected float maxLength = Float.MAX_VALUE;
	
	/*
	 * output
	 */
	
	protected List<VisibilityInteriorsLocation> sinks = new ArrayList<>();
	
	protected Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, VisibilityInteriorsPath>> paths = new HashMap<>();
	
	protected Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, Float>> sourceValues = new HashMap<>();
	
	protected Map<VisibilityInteriorsLocation, Float> sinkValues = new HashMap<>();
	
	/*
	 * render
	 */
	
	protected Map<VisibilityInteriorsConnection, Float> edges = new ConcurrentHashMap<>();
	
	protected Map<VisibilityInteriorsConnection, List<Float>> edgeValues = new ConcurrentHashMap<>();
	
	/*
	 * eval
	 */
		
	public VisibilityInteriorsEvaluation(String label, boolean onlyVisible, boolean onlyModifiable, boolean onlySingleFLoor, boolean onlyAccess) {
		this.label = label;
		this.onlyVisible = onlyVisible;
		this.onlyModifiable = onlyModifiable;
		this.onlySingleFloor = onlySingleFLoor;
		this.onlyAccess = onlyAccess;
	}
	
	public boolean isCumulator() {
		return this.isCumulator;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public void clear() {
						
		paths.clear();
		
		sourceValues.clear();
		sinkValues.clear();
		
		edges.clear();
		edgeValues.clear();
	}
	
	public void setMaxDistance(float maxDistance) {

		if (maxDistance > 0) {
			this.maxDistance = maxDistance;
		} else {
			this.maxDistance = Float.MAX_VALUE;
		}
	}
	
	public float getMaxDistance() {
		return this.maxDistance;
	}
	
	public void setMaxLength(float maxLength) {
		
		if (maxLength > 0) {
			this.maxLength = maxLength;
		} else {
			this.maxLength = Float.MAX_VALUE;
		}
	}
	
	public float getMaxLength() {
		return this.maxLength;
	}
	
	public void setSinks(List<VisibilityInteriorsLocation> sinks) {
		
		this.clear();
		this.sinks = sinks;
	}
	
	public Collection<VisibilityInteriorsLocation> getSinks() {
		return this.sinks;
	}
	
	public float getSinkValue (VisibilityInteriorsLocation location) {
		
		if (this.sinkValues.containsKey(location)) {
			return this.sinkValues.get(location);
		}
		
		return 0f;
		
	}
	
	public SortedMap<Float, Integer> getBinnedSinkValues() {
		
		SortedMap<Float, Integer> bins = new TreeMap<>();
		
		float[] bounds = this.getNodeBounds();
		
		for (int i = 0; i < 11; i++) {
			
			float val = ValueMapper.map(i, 0, 11, bounds[0], bounds[1]);
			
			bins.put(val, 0);
		}
		
		for (VisibilityInteriorsLocation sink : this.getSinks()) {
			
			Map.Entry<Float, Integer> min = null;
			
			for (Map.Entry<Float, Integer> bin : bins.entrySet()) {
						
				if (this.getSinkValue(sink) < bin.getKey()) {
					break;
				}	
				
				min = bin;
			}
			
			if (min == null) {
				bins.put(bins.firstKey(), bins.get(bins.firstKey()) + 1);
			} else {
				min.setValue(min.getValue() + 1);
			}
			
		}
		
		return bins;
	}
	
	public Map<VisibilityInteriorsLocation, Float> getSinkValues() {
		return this.sinkValues;
	}
	
	public void addSinkValue(VisibilityInteriorsLocation sink, float value) {
		
		if (value < minValue ) minValue = value;
		if (value > maxValue) maxValue = value;
		
		sinkValues.put(sink, value);
	}
	
	public Collection<VisibilityInteriorsLocation> getSources() {
		
		Set<VisibilityInteriorsLocation> sources = new HashSet<>();
		
		for (VisibilityInteriorsLocation sink : this.getSinks()) {
			sources.addAll(this.getSources(sink));
		}
		
		return new ArrayList<>(sources);
	}
	
	public Float getSourceValue(VisibilityInteriorsLocation source) {
		
		float value = 0f;
		float count = 0f;
		
		for (VisibilityInteriorsLocation sink : this.getSinks()) {
			if (this.getSources(sink).contains(source)) {
				value += this.getSourceValue(sink, source);
				count ++;
			}
		}
		
		if (count == 0) {
			return null;
		}
		
		if (this.isCumulator) {
			return value;
		} else {
			return value / count;
		}
	}
	
	public Collection<VisibilityInteriorsLocation> getSources(VisibilityInteriorsLocation sink) {
		
		if (this.sourceValues.containsKey(sink)) {
			return this.sourceValues.get(sink).keySet();
		}
		
		return new ArrayList<>();
	}
	
	public float getSourceValue (VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source) {
		return this.sourceValues.get(sink).get(source);
	}
	
	public Map<VisibilityInteriorsLocation, Float> getSourceValues(VisibilityInteriorsLocation sink) {
		
		if (this.sourceValues.containsKey(sink)) {
			return this.sourceValues.get(sink);
		}
		
		return new HashMap<>();
	}
		
	public void setSourceValue(VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source, float value, boolean includeInbounds) {
		
		if (!this.sourceValues.containsKey(sink)) {
			this.sourceValues.put(sink, new HashMap<>());
		}
		
		if (includeInbounds) {
			if (value < minValue ) minValue = value;
			if (value > maxValue) maxValue = value;
		}
		
		this.sourceValues.get(sink).put(source, value);
	}
		
	public VisibilityInteriorsPath getSourcePath(VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source) {
		return this.paths.get(sink).get(source);
	}
	
	public void setSourcePath(VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source, VisibilityInteriorsPath path) {	
		
		if (!this.paths.containsKey(sink)) {
			this.paths.put(sink, new HashMap<>());
		}
		
		this.paths.get(sink).put(source, path);
	}
	
	public Collection<VisibilityInteriorsConnection> getEdges() {
		return this.edges.keySet();
	}
	
	public float[] getNodeBounds() {
		return new float[] {minValue, maxValue};
	}
		
	public void addEdge(VisibilityInteriorsConnection edge, float count) {
		
		if (!edges.containsKey(edge)) {
			edges.put(edge, 0f);
		}
		
		edges.put(edge, edges.get(edge) + count);
	}
	
	public void addEdgeValue(VisibilityInteriorsConnection edge, float value, boolean includeInBounds) {
		
		if (!edgeValues.containsKey(edge)) {
			edgeValues.put(edge, new ArrayList<>());
		}
		
		if (includeInBounds) {
			if (value < minValue) minValue = value;
			if (value > maxValue) maxValue = value;
		}

		edgeValues.get(edge).add(value);
	}
	
	public float getEdgeCount (VisibilityInteriorsConnection connection) {
		
		if (this.edges.containsKey(connection)) {
			return this.edges.get(connection);
		}
				
		return 0f;
	}
	
	public float getEdgeValue (VisibilityInteriorsConnection connection) {
		
		float value = 0f;
		float count = 0f;
		
		VisibilityInteriorsLocation start = connection.getStartLocation();
		VisibilityInteriorsLocation end = connection.getEndLocation();
		
		Map<VisibilityInteriorsLocation, Float> startSources = this.sourceValues.get(start);
		Map<VisibilityInteriorsLocation, Float> endSources = this.sourceValues.get(end);
		
		if (startSources != null && endSources != null) {
			value += this.getSinkValue(connection.getStartLocation());
			value += this.getSinkValue(connection.getEndLocation());
		
			count += 2;
		
		} else {
			
			if (this.edgeValues.containsKey(connection)) {
				
				for (float edgeValue : this.edgeValues.get(connection)) {
					value += edgeValue;
					count ++;
				}
			}
		}

		return value / count;
	}
			
	public void evaluate() {
		
		System.err.println("evaluate should be overriden by a subclass of VisibilityInteriorsEvaluation");
	}
		
	public static VisibilityInteriorsEvaluation mergeEvaluations(String label, List<VisibilityInteriorsEvaluation> evaluations) {
		
		VisibilityInteriorsEvaluation merged = new VisibilityInteriorsEvaluation(label, false, false, false, false);
			
		Map<VisibilityInteriorsLocation, List<Float>> sinkValues = new HashMap<>();
		
		for (VisibilityInteriorsEvaluation evaluation : evaluations) {
			merged.paths.putAll(evaluation.paths);
			
			merged.maxDistance = evaluation.maxDistance;
			merged.maxLength = evaluation.maxLength;
			
			for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {
				
				if (!merged.sinks.contains(sink)) {
					merged.sinks.add(sink);
				}
				
				if (!merged.sourceValues.containsKey(sink)) {
					merged.sourceValues.put(sink, new HashMap<>());
				}
								
				merged.sourceValues.get(sink).putAll(evaluation.getSourceValues(sink));
				
				if (!sinkValues.containsKey(sink)) {
					sinkValues.put(sink, new ArrayList<>());
				}
				
				sinkValues.get(sink).add(evaluation.getSinkValue(sink));
			}
			
			for (VisibilityInteriorsConnection edge : evaluation.getEdges()) {
							
				if (!merged.edges.containsKey(edge)) {			
					merged.edges.put(edge, 0f);	
				}
				
				if (!merged.edgeValues.containsKey(edge)) {
					merged.edgeValues.put(edge, new ArrayList<>());
				}
				
				merged.edges.put(edge, merged.edges.get(edge) + evaluation.getEdgeCount(edge));
				merged.edgeValues.get(edge).addAll(evaluation.edgeValues.get(edge));
			}

			if (evaluation.isCumulator) merged.isCumulator = true;
		}
		
		float minValue = Float.MAX_VALUE;
		float maxValue = -Float.MIN_VALUE;
		
		for (Map.Entry<VisibilityInteriorsLocation, List<Float>> sinkValue : sinkValues.entrySet()) {
			
			float avg = 0f;
			
			for (Float val : sinkValue.getValue()) {
				avg += val / (float) sinkValue.getValue().size();
			}
			
			if (minValue > avg) minValue = avg;
			if (maxValue < avg) maxValue = avg;
			
			merged.sinkValues.put(sinkValue.getKey(), avg);
		}
		
		for (VisibilityInteriorsLocation source : merged.getSources()) {
			
			float avg = merged.getSourceValue(source);
			
			if (minValue > avg) minValue = avg;
			if (maxValue < avg) maxValue = avg;
			
		}
		
		merged.maxValue = maxValue;
		merged.minValue = minValue;
			
		return merged;
	}
}
