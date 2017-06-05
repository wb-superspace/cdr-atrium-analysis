package evaluations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import cdr.geometry.primitives.Point3D;
import javafx.legend.LegendItem;
import math.ValueMapper;
import templates.ModelEvaluation;
import templates.Serializable;

public abstract class EvaluationField implements Serializable, ModelEvaluation {

	HashMap<String, HashMap<Float, HashMap<Point3D, Float>>> values = new HashMap<>();
	HashMap<String, float[]> domains = new HashMap<>();
	HashMap<String, String> units = new HashMap<>();
	HashMap<String, ValueType> types = new HashMap<>();
	
	float numBins = 10;
	float resolution = 1f;
	
	@Override
	public Map<Object, Object> serialize() {

		Map<Object, Object> serialized = new HashMap<>();

		List<Object> evaluations = new ArrayList<>();
		
		for (String label : this.getValueLabels()) {
			
			Map<Object, Object> evaluation = new HashMap<>();
			
			evaluation.put("label", label);
			evaluation.put("unit", this.getUnit(label));
			evaluation.put("domain", this.getValueDomain(label));
			
			Map<Object, List<Object>> values = new HashMap<>();
			
			for (Float z : this.values.get(label).keySet()) {
				
				List<Object> arrays = new ArrayList<>();
				
				for (Point3D pt : this.values.get(label).get(z).keySet()) {
					
					List<Float> data = new ArrayList<>(4);
					
					data.add(pt.x());
					data.add(pt.y());
					data.add(pt.z());
					
					data.add(this.values.get(label).get(z).get(pt));
					
					arrays.add(data);
				}
				
				values.put(z, arrays);
			}
			
			evaluation.put("values", values);
			evaluations.add(evaluation);
		}
		
		serialized.put(this.getLabel(), evaluations);
		
		return serialized;
	}
	
	@Override
	public List<String> getValueLabels() {
		return new ArrayList<>(values.keySet());
	}

	@Override
	public String getUnit(String label) {
		return this.units.get(label);
	}
		
	@Override
	public ValueType getValueType(String label) {
		return this.types.get(label);
	}
	
	@Override
	public void clear() {
		this.values.clear();
		this.domains.clear();
		this.units.clear();
		this.types.clear();
	}

	public void setResolution(float resolution) {
		this.resolution = resolution;
	};
	
	public float getResoluation() {
		return this.resolution;
	}
	
	public Float getValue(String label, Float z, Point3D key) {
		return this.values.get(label).get(z).get(key);
	}
	
	public void setValue(String label, Float z, Point3D key, Float value, String unit, ValueType type) {
		
		if (!this.values.containsKey(label)) {
			this.values.put(label, new HashMap<>());
			this.units.put(label, unit);
			this.domains.put(label, new float[]{value, value});
			this.types.put(label, type);
		}
		
		if (!this.values.get(label).containsKey(z)) {
			this.values.get(label).put(z, new HashMap<>());
		}
		
		this.values.get(label).get(z).put(key, value);
		
		if (this.domains.get(label)[0] > value) {
			this.domains.get(label)[0] = value;
		}
		
		if (this.domains.get(label)[1] < value) {
			this.domains.get(label)[1] = value;
		}
	}
	
	public HashMap<Float, HashMap<Point3D, Float>> getValues(String label) {
		return this.values.get(label);
	}
	
	public HashMap<Point3D, Float> getValues(String label, Float z) {
		return this.values.get(label).get(z);
	}
	
	public float[] getValueDomain(String label) {
		return this.domains.get(label);
	}
	
	public void setValueDomain(String label, float[] domain) {
		this.domains.put(label, domain);
	}
	
	public SortedMap<Float, String> getValueLegend(String label, Collection<Float> zs, boolean normalized) {
		
		SortedMap<Float, String> valueLegend = new TreeMap<>();
		
		String unit = this.getUnit(label);
		float[] domain = this.getValueDomain(label);
		Collection<Float> values = new ArrayList<>();
		ValueType type = this.getValueType(label);
		
		Float min = null;
		Float max = null;
		
		for (Float z : this.values.get(label).keySet()) {
			
			for (Float val : this.values.get(label).get(z).values()) {
				
				if (zs.contains(z)) {
					
					if (min == null || val < min) {
						min = val;
					}
				
					if (max == null || val > max) {
						max = val;
					}
					
					values.add(val);
					
				} else {
					
					if (normalized) {
						
						values.add(val);
					}
				}
			}
		}
		
		if (normalized) {
			min = domain[0];
			max = domain[1];
		}
				
		switch (type) {
		
		case INTERPOLATE:
			
			for (int i=0; i<numBins; i++) {
				float mapped = ValueMapper.map(i, 0, numBins, min,  max);
				String text = Float.toString(mapped) + " " + unit;
				
				valueLegend.put(i/numBins, text);
			}
			
			break;
		
		case VALUE:
			
			for (float value : values) {				
				float mapped = ValueMapper.map(value, min, max, 0, 1);
				String text = Float.toString(value) + " " + unit;
				
				valueLegend.put(mapped, text);
			}
			
			break;
			
		case COUNT:
			
			for (float value : values) {				
				float mapped = ValueMapper.map(value, min, max, 0, 1);
				String text = Integer.toString((int)value) + " " + unit;
				
				valueLegend.put(mapped, text);
			}
			
			break;
			
		case PERCENTAGE:
						
			for (int i=0; i<numBins; i++) {
				float mapped = ValueMapper.map(i, 0, numBins, min,  max) * 100f;
				String text = Float.toString(mapped) + " " + unit;
				
				valueLegend.put(i/numBins, text);
			}
						
			break;
			
		default:
			
			break;
		}
		
		return valueLegend;
	}
}
