package com.unn.engine.dataset;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.functions.ValueTime;

public class InnerDataset implements Serializable {
	private static final long serialVersionUID = 4804115730789995484L;
	ArrayList<Integer> times;
	HashMap<IFunctor, HashMap<Integer, Integer>> timedValues;
	ArrayList<IFunctor> args;
	
	public InnerDataset() {
		this.times = new ArrayList<>();
		this.timedValues = new HashMap<>();
	}
	
	public void shrink() {
		this.times.clear();
		this.timedValues.clear();
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

	public ArrayList<Integer> getTimesByFunctor(IFunctor selector, Integer value) {
		ArrayList<Integer> times = getTimes();
		Collections.shuffle(times);
		return times.stream()
			.filter((time) -> getValueByTime(selector, time) == value)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public ArrayList<Integer> getTimesByFunctor(IFunctor selector, Integer value, ArrayList<Integer> seedTimes) {
		return seedTimes.stream()
			.filter((time) -> getValueByTime(selector, time) == value)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public Integer getValueByTime(IFunctor op, int time) {
		if (!this.timedValues.containsKey(op)) {
			return null;
		}
		return this.timedValues.get(op).get(time);
	}
	
	public ArrayList<IFunctor> getFunctors() {
		return this.args;
	}
	
	public void setFunctors(ArrayList<IFunctor> leaves) {
		this.args = leaves;
	}

	public HashMap<IFunctor, Integer> bundleSample(int time) {
		HashMap<IFunctor, Integer> input = new HashMap<>();
		for (IFunctor functor : getFunctors()) {
			Integer value = getValueByTime(functor, time);
			input.put(functor, value);
		}
		return input;
	}

	public InnerDataset copy() {
		InnerDataset dataset = new InnerDataset();
		dataset.times = (ArrayList<Integer>) this.times.clone();
		dataset.timedValues = new HashMap<>();
		for (Map.Entry<IFunctor,
				HashMap<Integer, Integer>> entry : timedValues.entrySet()) {
			dataset.timedValues.put(entry.getKey(),
					(HashMap<Integer, Integer>) entry.getValue().clone());
		}
		dataset.args = (ArrayList<IFunctor>) args.clone();
		return dataset;
	}
}
