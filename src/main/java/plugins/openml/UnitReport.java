package plugins.openml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import unn.structures.Config;

public class UnitReport implements Serializable {
	private static final long serialVersionUID = 7533264487477193079L;
	public final HashMap<String, OuterValueType> units;
	public String[] features;
	
	public UnitReport() {
		this.units = new HashMap<String, OuterValueType>();
	}
	
	public void addDiscreteSet(String feature, ArrayList<String> values) {
		Collections.sort(values);
		this.units.put(feature, new DiscreteSet(values));
	}
	
	public OuterValueType getValues(String feature) {
		return this.units.get(feature);
	}
	
	public void addNumeric(String feature, ArrayList<Double> values, Integer numericGroupCount) {
		NumericMapper mapper = new NumericMapper();
		// TODO: fix group count
		mapper.init(numericGroupCount != null ? numericGroupCount : 35, values);
		this.units.put(feature, mapper);
	}
	
	public Integer getInnerValue(String feature, String outerValue) {
		OuterValueType valueType = this.units.get(feature);
		
		if (valueType instanceof DiscreteSet) {
			DiscreteSet type = (DiscreteSet) valueType;
			
			if (type.cardinal() > Config.STIMULI_MAX_VALUE - Config.STIMULI_MIN_VALUE)  {
				System.err.println(String.format("Feature: %s value cardinality exceeds Cortex precision.", feature));
			}
			
			Integer index = type.getIndex(outerValue);
			
			if (index == null) {
				return null;
			}
			
			int innerValue = Config.STIMULI_MIN_VALUE;
			
			if (type.cardinal() > 1) {
				innerValue = (int) (Config.STIMULI_MIN_VALUE + index * Math.floor((Config.STIMULI_MAX_VALUE - Config.STIMULI_MIN_VALUE) / (type.cardinal() - 1)));
			}
			
			return innerValue;
		}
		else if (valueType instanceof NumericMapper) {
			if (outerValue == null) {
				return null;
			}
			NumericMapper mapper = (NumericMapper) valueType;
			return mapper.getInnerValue(Double.parseDouble(outerValue));
		}
		return null;
	}

	public void setFeatures(String[] features) {
		this.features = features;
	}
	
	public ArrayList<String> getFeatures() {
		ArrayList<String> features = new ArrayList<String>();
		if (this.units == null) {
			return features;
		}
		features.addAll(this.units.keySet());
		return features;
	}
}
