package com.unn.engine.metadata;

import com.unn.engine.Config;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.RandomManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MixedValuesDescriptor extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 1744475762857207392L;
	NumericValuesDescriptor numericDescriptor;
	DiscreteValuesDescriptor discreteDescriptor;

	public MixedValuesDescriptor() { }
	
	public void init(int groupCount, ArrayList<Double> numerics, ArrayList<String> labels) {
		this.numericDescriptor = new NumericValuesDescriptor();
		this.numericDescriptor.init(groupCount, numerics);
		this.discreteDescriptor = new DiscreteValuesDescriptor();
		this.discreteDescriptor.init(labels);
	}

	@Override
	public ArrayList<String> getGroups(String suffix) {
		ArrayList<String> discreteGroup = this.discreteDescriptor.getGroups(suffix);
		ArrayList<String> numericGroup = this.numericDescriptor.getGroups(suffix);
		ArrayList<String> fused = new ArrayList<>();
		fused.addAll(discreteGroup);
		fused.addAll(numericGroup);
		return fused;
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		Raw raw = new Raw();
		raw.setDescriptor(new FunctionDescriptor(group));
		return raw;
	}

	@Override
	public ArrayList<String> getGroupByOuterValue(String outerFeatureValue, String suffix) {
		try {
			Double.parseDouble(outerFeatureValue);
			return this.numericDescriptor.getGroupByOuterValue(outerFeatureValue, suffix);
		} catch (Exception e) {
			return this.discreteDescriptor.getGroupByOuterValue(outerFeatureValue, suffix);
		}
	}
}
