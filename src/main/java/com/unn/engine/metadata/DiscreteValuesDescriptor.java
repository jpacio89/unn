package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.SimpleFunctor;
import com.unn.engine.interfaces.IFunctor;

public class DiscreteValuesDescriptor extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 2644249077021570502L;
	private ArrayList<String> values;
	
	public DiscreteValuesDescriptor() {
		super();
	}

	public DiscreteValuesDescriptor(String _suffix) {
		super(_suffix);
	}

	public void init(ArrayList<String> _values) {
		this.values = _values;
	}

	@Override
	public ArrayList<String> getGroups() {
		return this.values.stream()
			.map(value -> String.format("discrete_%s_%s", value, getSuffix()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		SimpleFunctor simpleFunctor = new SimpleFunctor();
		String name = String.format("%s", group);
		simpleFunctor.setDescriptor(new FunctionDescriptor(name));
		return simpleFunctor;
	}

	@Override
	public ArrayList<String> getGroupByOuterValue(String outerFeatureValue) {
		ArrayList response = new ArrayList<>();
		if (!this.values.contains(outerFeatureValue)) {
			response.add(String.format("discrete_labelized_int_%s_%s", outerFeatureValue, getSuffix()));
			return response;
		}
		response.add(String.format("discrete_%s_%s", outerFeatureValue, getSuffix()));
		return response;
	}
}