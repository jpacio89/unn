package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.unn.engine.Config;
import com.unn.engine.functions.SimpleFeature;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.RandomManager;

public class NumericValuesDescriptor extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 1744475762857207392L;
	public int groupCount;
	public ArrayList<Double> possibleValues;
	HashMap<String, Pair<Double, Boolean>> groups;
	double minValue;
	double maxValue;

	public NumericValuesDescriptor() {
		super();
	}

	public NumericValuesDescriptor(String _suffix) {
		super(_suffix);
	}

	public void init(int groupCount, ArrayList<Double> possibleValues) {
		this.groups = new HashMap<>();
		this.groupCount = groupCount;
		this.possibleValues = new ArrayList<>(possibleValues);
		this.minValue = Collections.min(this.possibleValues);
		this.maxValue = Collections.max(this.possibleValues);
		createGroups();
	}

	void addGroup(double value, boolean isInverted) {
		String name = String.format("numeric_%f_%b", value, isInverted);
		this.groups.put(name, new Pair<>(value, isInverted));
	}

	void createGroups() {
		this.addGroup(this.minValue, true);
		this.addGroup(this.maxValue, false);
		for (int i = 0; i < this.groupCount; ++i) {
			double midPoint = RandomManager.getOne(this.possibleValues);
			boolean isInverted = RandomManager.getBoolean();
			this.addGroup(midPoint, isInverted);
		}
	}

	@Override
	public ArrayList<String> getOutputFeatures() {
		return this.groups.keySet().stream()
			.map(group -> String.format("%s_%s", group, getSuffix()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public IFeature getFeatureByName(String name) {
		SimpleFeature simpleFunctor = new SimpleFeature();
		simpleFunctor.setName(name);
		return simpleFunctor;
	}

	@Override
	public ArrayList<String> getActivatedOutputFeatures(String outerValue) {
		double parsedValue = Double.parseDouble(outerValue);
		return this.groups.keySet().stream()
			.filter(group -> getInnerValue(group, parsedValue) == Config.STIM_MAX)
			.map(group -> String.format("%s_%s", group, getSuffix()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private Integer getInnerValue(String groupName, double value) {
		Pair<Double, Boolean> params = this.groups.get(groupName);
		return threshold(value, params.first(), params.second()) ?
			Config.STIM_MAX : Config.STIM_MIN;
	}

	boolean threshold (double value, double thresholdPoint, boolean inverted) {
		if (value >= thresholdPoint) {
			return !inverted;
		}
		return inverted;
	}
}
