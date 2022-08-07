package com.unn.engine.metadata;

import com.unn.engine.functions.SimpleFeature;
import com.unn.engine.interfaces.IFeature;

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
	public ArrayList<String> getOutputFeatures() {
		ArrayList<String> discreteGroup = this.discreteDescriptor.getOutputFeatures();
		ArrayList<String> numericGroup = this.numericDescriptor.getOutputFeatures();
		ArrayList<String> fused = new ArrayList<>();
		fused.addAll(discreteGroup);
		fused.addAll(numericGroup);
		return fused;
	}

	@Override
	public IFeature getFeatureByName(String name) {
		SimpleFeature simpleFunctor = new SimpleFeature();
		simpleFunctor.setName(name);
		return simpleFunctor;
	}

	@Override
	public ArrayList<String> getActivatedOutputFeatures(String outerValue) {
		try {
			Double.parseDouble(outerValue);
			return this.numericDescriptor.getActivatedOutputFeatures(outerValue);
		} catch (Exception e) {
			return this.discreteDescriptor.getActivatedOutputFeatures(outerValue);
		}
	}
}
