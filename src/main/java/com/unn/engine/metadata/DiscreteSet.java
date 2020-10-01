package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.Config;

public class DiscreteSet extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 2644249077021570502L;
	public final ArrayList<String> values;
	
	public DiscreteSet(ArrayList<String> _values) {
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
	
	public ArrayList<Integer> getAllInnerValues() {
		ArrayList<Integer> innerValues = new ArrayList<Integer>();
		
		if (cardinal() == 1) {
			innerValues.add(Config.STIMULI_MIN_VALUE);
		} else {
			for (int i = 0; i < this.values.size(); ++i) {
				int innerValue = (int) (Config.STIMULI_MIN_VALUE + i * Math.floor((Config.STIMULI_MAX_VALUE - Config.STIMULI_MIN_VALUE) / (cardinal() - 1)));
				innerValues.add(innerValue);
			}	
		}
		
		return innerValues;
	}
}