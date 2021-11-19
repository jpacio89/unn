package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.unn.engine.Config;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.mining.PerformanceAnalyzer;

public class Model implements Serializable {
	private static final long serialVersionUID = 6696314435525368206L;

	final int TEST_SAMPLE_COUNT = 1000;
	
	PerformanceAnalyzer performanceAnalyzer;
	InnerDataset dataset;
	ArrayList<Predicate> predicates;
	IFeature rewardSelector;

	public Model(InnerDataset dataset, IFeature rewardSelector) {
		this.dataset = dataset;
		this.predicates = new ArrayList<>();
		this.performanceAnalyzer = new PerformanceAnalyzer();
		this.rewardSelector = rewardSelector;
	}
	
	public Model(InnerDataset dataset, ArrayList<Predicate> sublist, IFeature rewardSelector) {
		this.dataset = dataset;
		this.predicates = sublist;
		this.rewardSelector = rewardSelector;
	}
	
	public void add(Predicate predicate) {
		this.predicates.add(predicate);
	}

	public void sort() {
		this.predicates.sort(Comparator.comparingInt(x -> -x.targetTimes.size()));
	}
	
	public PerformanceAnalyzer getPerformanceAnalyzer() {
		return this.performanceAnalyzer;
	}
	
	public void calculatePerformance(ArrayList<Integer> testTimes) {
		this.performanceAnalyzer = new PerformanceAnalyzer();
		Collections.shuffle(testTimes);
		testTimes = new ArrayList<> (testTimes.subList(0, Math.min(TEST_SAMPLE_COUNT, testTimes.size())));

		for (Integer time : testTimes) {
			predict(time, performanceAnalyzer);
		}
	}

	private boolean predict (int time, PerformanceAnalyzer analyzer) {
		HashMap<IFeature, Integer> inputs = this.getInputsByTime(time);
		Double prediction = this.predict(inputs);
		double adjustedPrediction = prediction == null ? Config.get().STIM_NULL: prediction.doubleValue();
		int historicAction = this.dataset.getValueByTime(this.rewardSelector, time);

		try {
			analyzer.addEvent(historicAction, adjustedPrediction);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return adjustedPrediction != Config.get().STIM_NULL;
	}
	
	public Double predict(int time) {
		HashMap<IFeature, Integer> inputs = this.getInputsByTime(time);
		return this.predict(inputs);
	}
	
	public Double predict(HashMap<IFeature, Integer> inputs) {
		double accumulator = 0.0;
		long activationCount = 0;
			
		for (int i = 0; i < this.predicates.size(); ++i) {
			Predicate predicate = this.predicates.get(i);

			if (!this.isActivation(inputs, i)) {
				continue;
			}

			accumulator += predicate.reward;
			activationCount++;

			if (activationCount >= Config.get().MODEL_PREDICTION_PREDICATE_HIT_COUNT) {
				break;
			}
		}
		
		if (activationCount == 0) {
			//return (double) Config.get().STIM_NULL;
			return null;
		}
		
		accumulator /= activationCount;
		
		return accumulator;
	}
	
	public Boolean isActivation (HashMap<IFeature, Integer> inputs, int artifactIndex) {
		boolean activated = true;
		Predicate predicate = this.predicates.get(artifactIndex);
		
		for (Predicate.Condition condition : predicate.conditions) {
			try {
				if (inputs.get(condition.feature) != condition.activationValue) {
					activated = false;
					break;
				}
			} catch (Exception e) {
				activated = false;
				//e.printStackTrace();
			}
		}
		
		return activated;
	}

	public boolean isEmpty() {
		return this.getPredicates().size() == 0;
	}

	public ArrayList<IFeature> getInputs() {
		return this.dataset.getFunctors();
	}
	
	public ArrayList<Predicate> getPredicates() {
		return this.predicates;
	}

	private HashMap<IFeature, Integer> getInputsByTime(int time) {
		HashMap<IFeature, Integer> inputs = new HashMap<>();

		for (IFeature param : this.dataset.getFunctors()) {
			Integer val = this.dataset.getValueByTime(param, time);
			inputs.put(param, val);
		}

		return inputs;
	}
}
