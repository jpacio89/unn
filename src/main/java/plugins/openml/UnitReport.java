package plugins.openml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import unn.Config;

public class UnitReport {
	HashMap<String, OuterValueType> units;
	
	public UnitReport() {
		this.units = new HashMap<String, OuterValueType>();
	}
	
	public void addDiscreteSet(String feature, ArrayList<String> values) {
		Collections.sort(values);
		this.units.put(feature, new DiscreteSet(values));
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
	
	private class OuterValueType {}
	
	private class DiscreteSet extends OuterValueType {
		ArrayList<String> values;
		
		public DiscreteSet(ArrayList _values) {
			this.values = _values;
		}
		
		public Integer getIndex(String _value) {
			int index = this.values.indexOf(_value);
			if (index < 0) {
				return null;
			}
			return index;
		}
		
		public int cardinal() {
			return this.values.size();
		}
	}
	
	private class Range extends OuterValueType {
		double lb;
		double ub;
		
		public Range(double lb, double ub) {
			this.lb = lb;
			this.ub = ub;
		}
	}
	
	
	
	
}
