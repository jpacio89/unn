package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.Config;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.interfaces.IFunctor;

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
		ArrayList<Integer> innerValues = new ArrayList<>();
		if (cardinal() == 1) {
			innerValues.add(Config.STIM_MIN);
		} else {
			for (int i = 0; i < this.values.size(); ++i) {
				int innerValue = (int) (Config.STIM_MIN + i * Math.floor((Config.STIM_MAX - Config.STIM_MIN) / (cardinal() - 1)));
				innerValues.add(innerValue);
			}	
		}
		return innerValues;
	}

	@Override
	public ArrayList<String> getGroups() {
		return this.values;
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		Raw raw = new Raw();
		raw.setDescriptor(new FunctionDescriptor(".", group,-1));
		return raw;
	}

	@Override
	public String getGroupByOuterValue(String outerFeatureValue) {
		return outerFeatureValue;
	}
}