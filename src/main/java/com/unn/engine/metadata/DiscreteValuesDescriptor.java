package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.unn.engine.Config;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.interfaces.IFunctor;

public class DiscreteValuesDescriptor extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 2644249077021570502L;
	private ArrayList<String> values;
	
	public DiscreteValuesDescriptor() { }

	public void init(ArrayList<String> _values) {
		this.values = _values;
	}

	@Override
	public ArrayList<String> getGroups(String suffix) {
		return this.values.stream()
			.map(value -> String.format("discrete_%s_%s", value, suffix))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		Raw raw = new Raw();
		String name = String.format("%s", group);
		raw.setDescriptor(new FunctionDescriptor(".", name,-1));
		return raw;
	}

	@Override
	public ArrayList<String> getGroupByOuterValue(String outerFeatureValue, String suffix) {
		ArrayList response = new ArrayList<>();
		if (!this.values.contains(outerFeatureValue)) {
			response.add(String.format("discrete_labelized_int_%s_%s", outerFeatureValue, suffix));
			return response;
		}
		response.add(String.format("discrete_%s_%s", outerFeatureValue, suffix));
		return response;
	}
}