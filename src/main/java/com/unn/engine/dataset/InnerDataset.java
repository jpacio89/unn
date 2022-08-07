package com.unn.engine.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.unn.engine.interfaces.IFeature;
import com.unn.engine.functions.ValueTime;

public class InnerDataset implements Serializable {
	private static final long serialVersionUID = 4804115730789995484L;
	ArrayList<Integer> times;
	HashMap<IFeature, HashMap<Integer, Integer>> timedValues;
	ArrayList<IFeature> args;
	
	public InnerDataset() {
		this.times = new ArrayList<>();
		this.timedValues = new HashMap<>();
	}
	
	public void shrink() {
		this.times.clear();
		this.timedValues.clear();
		this.args.clear();
	}
	
	public void add(ValueTime vtr) {
		assert vtr.getClass() != null &&
			vtr.getValue() != null &&
			vtr.getTime() != null;
		
		if (!this.times.contains(vtr.getTime())) {
			this.times.add(vtr.getTime());
		}

		if (!this.timedValues.containsKey (vtr.getVTRClass())) {
			HashMap<Integer, Integer> classValues = new HashMap<>();
			classValues.put (vtr.getTime(), vtr.getValue());
			this.timedValues.put (vtr.getVTRClass(), classValues);
		} else {
			this.timedValues.get (vtr.getVTRClass()).put(vtr.getTime(), vtr.getValue());
		}
	}

	public ArrayList<Integer> getTimes() {
		return this.times;
	}

	public ArrayList<Integer> getTimesByFunctor(IFeature selector, Integer value) {
		ArrayList<Integer> times = getTimes();
		Collections.shuffle(times);
		return times.stream()
			.filter((time) -> getValueByTime(selector, time) == value)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public ArrayList<Integer> getTimesByFunctor(IFeature selector, Integer value, ArrayList<Integer> seedTimes) {
		return seedTimes.stream()
			.filter((time) -> getValueByTime(selector, time) == value)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public Integer getValueByTime(IFeature op, int time) {
		if (!this.timedValues.containsKey(op)) {
			System.err.println("|InnerDataset| Requested time does not have value");
			return null;
		}
		return this.timedValues.get(op).get(time);
	}
	
	public ArrayList<IFeature> getFunctors() {
		return this.args;
	}
	
	public void setFunctors(ArrayList<IFeature> leaves) {
		this.args = leaves;
	}

	public HashMap<IFeature, Integer> bundleSample(int time) {
		HashMap<IFeature, Integer> input = new HashMap<>();
		for (IFeature functor : getFunctors()) {
			Integer value = getValueByTime(functor, time);
			input.put(functor, value);
		}
		return input;
	}

	public InnerDataset copy() {
		InnerDataset dataset = new InnerDataset();
		dataset.times = (ArrayList<Integer>) this.times.clone();
		dataset.timedValues = new HashMap<>();
		for (Map.Entry<IFeature,
				HashMap<Integer, Integer>> entry : timedValues.entrySet()) {
			dataset.timedValues.put(entry.getKey(),
					(HashMap<Integer, Integer>) entry.getValue().clone());
		}
		dataset.args = (ArrayList<IFeature>) args.clone();
		return dataset;
	}

	public void inject(InnerDataset realtimeDataset) {
		this.times.clear();
		this.times.addAll(realtimeDataset.times);
		this.timedValues.clear();
		this.timedValues.putAll(realtimeDataset.timedValues);
		this.args.clear();
		this.args.addAll(realtimeDataset.args);
	}
}
