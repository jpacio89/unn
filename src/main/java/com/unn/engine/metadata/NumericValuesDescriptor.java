package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.unn.engine.dataset.FeatureValueHistogram;
import com.unn.engine.Config;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.RandomManager;

public class NumericValuesDescriptor extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 1744475762857207392L;
	public int groupCount;
	public ArrayList<Double> possibleValues;
	HashMap<String, Pair<Double, Boolean>> groups;
	double minValue;
	double maxValue;

	public NumericValuesDescriptor() { }
	
	public void init(int groupCount, ArrayList<Double> possibleValues) {
		this.groups = new HashMap<>();
		this.groupCount = groupCount;
		this.possibleValues = new ArrayList<>(possibleValues);
		this.minValue = Collections.min(this.possibleValues);
		this.maxValue = Collections.max(this.possibleValues);
		createGroups();
	}

	void createGroups() {
		for (int i = 0; i < this.groupCount; ++i) {
			double midPoint = RandomManager.getOne(this.possibleValues);
			boolean isInverted = RandomManager.getBoolean();
			String name = String.format("numeric_%f_%d", midPoint, isInverted);
			this.groups.put(name, new Pair<>(midPoint, isInverted));
		}
	}

	@Override
	public ArrayList<String> getGroups(String suffix) {
		return this.groups.keySet().stream()
			.map(group -> String.format("%s_%s", group, suffix))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		Raw raw = new Raw();
		raw.setDescriptor(new FunctionDescriptor(".", group,-1));
		return raw;
	}

	@Override
	public ArrayList<String> getGroupByOuterValue(String outerFeatureValue, String suffix) {
		double parsedValue = Double.parseDouble(outerFeatureValue);
		return this.groups.keySet().stream()
			.filter(group -> getInnerValue(group, parsedValue) == Config.STIM_MAX)
			.map(group -> String.format("%s_%s", group, suffix))
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