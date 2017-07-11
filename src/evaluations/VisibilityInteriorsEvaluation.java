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

import com.sun.swing.internal.plaf.metal.resources.metal_zh_TW;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

import math.ValueMapper;
import models.isovistProjectionModel.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.VisibilityInteriorsConnection;
import models.visibilityInteriorsModel.types.VisibilityInteriorsLocation;
import models.visibilityInteriorsModel.types.VisibilityInteriorsPath;
import search.LookupGridManager;

public class VisibilityInteriorsEvaluation {
		
	public enum EvaluationType {
		ACCESSIBILITY,
		EXPOSURE,
		VISIBILITY,
		DISTANCE
	}
	
	private String label = null;
	private EvaluationType type = null;
	
	protected boolean isCumulator = false;
	
	protected float minValue = Float.MAX_VALUE;
	protected float maxValue = -Float.MAX_VALUE;
	
	protected float maxDistance = Float.MAX_VALUE;
	protected float maxLength = Float.MAX_VALUE;
	
	/*
	 * output
	 */
	
	private SortedMap<Float, List<IsovistProjectionPolygon>> projections = new TreeMap<>(); // was LookupGridManager;
	
	protected List<VisibilityInteriorsLocation> sinks = new ArrayList<>();
	
	protected List<VisibilityInteriorsLocation> sources = new ArrayList<>();
	
	protected Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, VisibilityInteriorsPath>> paths = new ConcurrentHashMap<>();
	
	protected Map<VisibilityInteriorsLocation, Map<VisibilityInteriorsLocation, Float>> sourceValues = new ConcurrentHashMap<>();
	
	protected Map<VisibilityInteriorsLocation, Float> sinkValues = new ConcurrentHashMap<>();
	
	/*
	 * render
	 */
	
	protected Map<VisibilityInteriorsConnection, Float> edges = new ConcurrentHashMap<>();
	
	/*
	 * eval
	 */
		
	public VisibilityInteriorsEvaluation(String label, EvaluationType type) {
		this.label = label;
		this.type = type;
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
		
		this.projections.clear();
		
		for (VisibilityInteriorsLocation sink : sinks) {
			for (Map.Entry<Float, List<IsovistProjectionPolygon>> projectionEntry : sink.getProjectionPolygons().entrySet()) {	
				
				if (!this.projections.containsKey(projectionEntry.getKey())) {
					this.projections.put(projectionEntry.getKey(), new ArrayList<>());
				}
				
				if (projectionEntry.getKey() <= sink.getLayout().getAnchor().z()) {
					
					for (IsovistProjectionPolygon projectionPolygon : projectionEntry.getValue()) {
						this.projections.get(projectionEntry.getKey()).add(projectionPolygon);
					}
				}
			}
		}
	}
	
	public Collection<VisibilityInteriorsLocation> getSinks() {
		return this.sinks;
	}
		
	public Float getSinkValue (VisibilityInteriorsLocation location) {
		
		if (this.sinkValues.containsKey(location)) {
			return this.sinkValues.get(location);
		}
		
		return null;	
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
	
	public void setSources(List<VisibilityInteriorsLocation> sources) {
		
		this.clear();
		this.sources = sources;
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
		
		return new ConcurrentHashMap<>();
	}
		
	public void setSourceValue(VisibilityInteriorsLocation sink, VisibilityInteriorsLocation source, float value, boolean includeInbounds) {
		
		if (!this.sourceValues.containsKey(sink)) {
			this.sourceValues.put(sink, new ConcurrentHashMap<>());
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
		
	public float[] getProjectionOverlapBounds() {
		
		float[] bounds = new float[]{0, -Float.MAX_VALUE};
		
		for (List<IsovistProjectionPolygon> manager : this.projections.values()) {			
			if (manager.size() > bounds[1]) bounds[1] = (float) manager.size();
		}
		
		bounds[1] = (float) Math.sqrt(bounds[1]);
		
		return bounds;
	}
		
	public void addEdge(VisibilityInteriorsConnection edge, float count) {
		
		if (!edges.containsKey(edge)) {
			edges.put(edge, 0f);
		}
		
		edges.put(edge, edges.get(edge) + count);
	}
	
	
	public float getEdgeCount (VisibilityInteriorsConnection connection) {
		
		if (this.edges.containsKey(connection)) {
			return this.edges.get(connection);
		}
				
		return 0f;
	}
			
	public void evaluate() {
		
		switch (this.type) {
		
		case ACCESSIBILITY:
			isCumulator = false;
			evaluateAccessibility();
			break;
			
		case DISTANCE:
			isCumulator = false;
			evaluateDistance();
			break;
		
		case EXPOSURE:
			isCumulator = true;
			evaluateExposure();
			break;
			
		case VISIBILITY:
			isCumulator = true;
			evaluateVisibility();
			break;

		default:
			break;
		}
	}
	
	public void evaluateAccessibility() {

		Set<VisibilityInteriorsLocation> sources = new HashSet<>();
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
		
		this.clear();
	
		for (VisibilityInteriorsLocation sink : sinks) {			
			for (VisibilityInteriorsLocation source : this.sources) {
				if (!sink.equals(source)) {
					if (sink.getDistance(source) <= maxDistance &&
						sink.getConnectivityPath(source).getLength() <= maxLength) {
						sources.add(source);
					}	
				}		
			}
		}
						
		for (VisibilityInteriorsLocation source : sources) {
			
			VisibilityInteriorsPath minPath = null;
			VisibilityInteriorsLocation minSink = null;
			
			for (VisibilityInteriorsLocation location : sinks) {
				
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
						
			setSourcePath(minSink, source, minPath);
			setSourceValue(minSink, source, minPath.getAccessibility(), true);
			
			for (VisibilityInteriorsConnection connection : minPath.getConnections()) {
								
				addEdge(connection, 1);	
			}
		}
		
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float average = 0f;
			
			for (float value : getSourceValues(sink).values()) {
				average += value / (float) getSourceValues(sink).size();
			}
			
			addSinkValue(sink, average);
		}
	}
	
	public void evaluateDistance() {

		Set<VisibilityInteriorsLocation> sources = new HashSet<>();
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
		
		this.clear();
	
		for (VisibilityInteriorsLocation sink : sinks) {			
			for (VisibilityInteriorsLocation source : this.sources) {
				if (!sink.equals(source)) {
					if (sink.getDistance(source) <= maxDistance &&
						sink.getConnectivityPath(source).getLength() <= maxLength) {
						sources.add(source);
					}	
				}		
			}
		}
						
		for (VisibilityInteriorsLocation source : sources) {
			
			VisibilityInteriorsPath minPath = null;
			VisibilityInteriorsLocation minSink = null;
			
			for (VisibilityInteriorsLocation location : sinks) {
				
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
						
			setSourcePath(minSink, source, minPath);
			setSourceValue(minSink, source, minPath.getLength(), true);
			
			for (VisibilityInteriorsConnection connection : minPath.getConnections()) {
								
				addEdge(connection, 1);		
			}
		}
		
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float average = 0f;
			
			for (float value : getSourceValues(sink).values()) {
				average += value / (float) getSourceValues(sink).size();
			}
			
			addSinkValue(sink, average);
		}
	}
	
	public void evaluateExposure() {
		
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
		
		this.clear();
	
		for (VisibilityInteriorsLocation sink : sinks) {
			
			Set<VisibilityInteriorsLocation> sources = new HashSet<>();
						
			for (VisibilityInteriorsLocation source : this.sources) {
				if (!sink.equals(source)) {
					if (sink.getDistance(source) <= maxDistance &&
						sink.getConnectivityPath(source).getLength() <= maxLength) {
						sources.add(source);
					}	
				}
			}
			
			for (VisibilityInteriorsLocation source : sources) {
				
				VisibilityInteriorsPath path = sink.getConnectivityPath(source);
				
				setSourcePath(sink, source, path);
				setSourceValue(sink, source, 1f, true);
				
				for (VisibilityInteriorsConnection connection : path.getConnections()) {
														
					addEdge(connection, 1);			
				}
			}
		}
								
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float sum = 0f;
			
			for (float value : getSourceValues(sink).values()) {
				sum += value;
			}
			
			addSinkValue(sink, sum);
		}
	}
	
	public void evaluateVisibility() {
		
		this.clear();
		
		Set<VisibilityInteriorsLocation> sinks = new HashSet<>(this.sinks);
										
		for (VisibilityInteriorsLocation sink : sinks) {
			
			float sum = 0f;
			
			for (List<IsovistProjectionPolygon> projectionPolygons : sink.getProjectionPolygons().values()) {
				for (IsovistProjectionPolygon projectionPolygon : projectionPolygons) {
					sum += projectionPolygon.getPolygon3DWithHoles().area();
				}
			}
			
			for (VisibilityInteriorsLocation connection : sink.getConnectivityPathLocations()) {
				
				VisibilityInteriorsPath path = sink.getConnectivityPath(connection);
				
				if (path.getConnections().size() == 1) {
					
					VisibilityInteriorsConnection c = path.getConnections().get(0); 
																		
					addEdge(c, sum);
				}
			}
			
			addSinkValue(sink, sum);
		}
	}
		
	public static VisibilityInteriorsEvaluation mergeEvaluations(String label, List<VisibilityInteriorsEvaluation> evaluations) {
		
		VisibilityInteriorsEvaluation merged = new VisibilityInteriorsEvaluation(label, null);
			
		Map<VisibilityInteriorsLocation, List<Float>> sinkValues = new ConcurrentHashMap<>();
		
		merged.projections = new TreeMap<>();
		
		for (VisibilityInteriorsEvaluation evaluation : evaluations) {
			merged.paths.putAll(evaluation.paths);
			
			merged.maxDistance = evaluation.maxDistance;
			merged.maxLength = evaluation.maxLength;
						
			for (Map.Entry<Float, List<IsovistProjectionPolygon>> projectionEntry : evaluation.projections.entrySet()) {
				
				if (!merged.projections.containsKey(projectionEntry.getKey())) {
					merged.projections.put(projectionEntry.getKey(), new ArrayList<>());
				}
				
				merged.projections.get(projectionEntry.getKey()).addAll(projectionEntry.getValue());
			}
			
			for (VisibilityInteriorsLocation sink : evaluation.getSinks()) {
				
				if (!merged.sinks.contains(sink)) {
					merged.sinks.add(sink);
				}
				
				if (!merged.sourceValues.containsKey(sink)) {
					merged.sourceValues.put(sink, new ConcurrentHashMap<>());
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
				
				merged.edges.put(edge, merged.edges.get(edge) + evaluation.getEdgeCount(edge));
			}

			if (evaluation.isCumulator) merged.isCumulator = true;
		}
		
		float minValue = Float.MAX_VALUE;
		float maxValue = -Float.MIN_VALUE;
		
		for (Map.Entry<VisibilityInteriorsLocation, List<Float>> sinkValue : sinkValues.entrySet()) {
			
			float avg = 0f;
			
			for (Float val : sinkValue.getValue()) {
				
				avg += val == null ? 0 : val / (float) sinkValue.getValue().size();
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
