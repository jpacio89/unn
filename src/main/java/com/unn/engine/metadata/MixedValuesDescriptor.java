package com.unn.engine.metadata;

import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.SimpleFunctor;
import com.unn.engine.interfaces.IFunctor;

import java.io.Serializable;
import java.util.ArrayList;

public class MixedValuesDescriptor extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 1744475762857207392L;
	NumericValuesDescriptor numericDescriptor;
	DiscreteValuesDescriptor discreteDescriptor;

	public MixedValuesDescriptor() {
		super();
	}
	
	public void init(int groupCount, ArrayList<Double> numerics, ArrayList<String> labels) {
		this.numericDescriptor = new NumericValuesDescriptor(getSuffix());
		this.numericDescriptor.init(groupCount, numerics);
		this.discreteDescriptor = new DiscreteValuesDescriptor(getSuffix());
		this.discreteDescriptor.init(labels);
	}

	@Override
	public ArrayList<String> getGroups() {
		ArrayList<String> discreteGroup = this.discreteDescriptor.getGroups();
		ArrayList<String> numericGroup = this.numericDescriptor.getGroups();
		ArrayList<String> fused = new ArrayList<>();
		fused.addAll(discreteGroup);
		fused.addAll(numericGroup);
		return fused;
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		SimpleFunctor simpleFunctor = new SimpleFunctor();
		simpleFunctor.setDescriptor(new FunctionDescriptor(group));
		return simpleFunctor;
	}

	@Override
	public ArrayList<String> getGroupByOuterValue(String outerFeatureValue) {
		try {
			Double.parseDouble(outerFeatureValue);
			return this.numericDescriptor.getGroupByOuterValue(outerFeatureValue);
		} catch (Exception e) {
			return this.discreteDescriptor.getGroupByOuterValue(outerFeatureValue);
		}
	}
}
