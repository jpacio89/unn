package plugins.openml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import unn.structures.Config;

public class UnitReport {
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
	
	public void addRange(String feature, double lb, double ub) {
		this.units.put(feature, new Range(lb, ub));
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
			
			int innerValue = (int) (Config.STIMULI_MIN_VALUE + index * Math.floor((Config.STIMULI_MAX_VALUE - Config.STIMULI_MIN_VALUE) / (type.cardinal() - 1)));
			return innerValue;
		} 
		else if (valueType instanceof Range) {
			// TODO: implement
			return null;
		}
		
		return null;
	}
	
	private class Range extends OuterValueType {
		// public final double lb;
		// public final double ub;
		
		public Range(double lb, double ub) {
			// this.lb = lb;
			// this.ub = ub;
		}
	}

	public void setFeatures(String[] features) {
		this.features = features;
		
	}
	
	
	
	
}
