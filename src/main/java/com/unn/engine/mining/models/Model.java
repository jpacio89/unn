package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.unn.engine.Config;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.mining.PerformanceAnalyzer;

public class Model implements Serializable {
	private static final long serialVersionUID = 6696314435525368206L;

	final int TEST_SAMPLE_COUNT = 1000;
	
	PerformanceAnalyzer walker;
	InnerDataset dataset;
	ArrayList<Predicate> predicates;
	IFeature rewardSelector;

	public Model(InnerDataset dataset, IFeature rewardSelector) {
		this.dataset = dataset;
		this.predicates = new ArrayList<>();
		this.walker = new PerformanceAnalyzer();
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
	
	public PerformanceAnalyzer getStatsWalker() {
		return this.walker;
	}
	
	public void calculatePerformance(ArrayList<Integer> testTimes) {
		this.walker = new PerformanceAnalyzer();
		Collections.shuffle(testTimes);
		testTimes = new ArrayList<> (testTimes.subList(0, Math.min(TEST_SAMPLE_COUNT, testTimes.size())));
		for (Integer time : testTimes) {
			predict(time, walker);
		}
	}

	private boolean predict (int time, PerformanceAnalyzer walker) {
		HashMap<IFeature, Integer> inputs = this.getInputsByTime(time);
		Double prediction = this.predict(inputs);
		double adjustedPrediction = prediction == null ? Config.get().STIM_NULL: prediction.doubleValue();
		int historicAction = this.dataset.getValueByTime(this.rewardSelector, time);
		try {
			walker.addEvent(historicAction, adjustedPrediction);
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
		double rewardAccumulator = 0.0;
		long hitCount = 0;
			
		for (int i = 0; i < this.predicates.size(); ++i) {
			Predicate predicate = this.predicates.get(i);
			boolean isHit = this.isHit(inputs, i);

			if (!isHit) {
				continue;
			}

			rewardAccumulator += isHit ? predicate.reward : Config.get().STIM_NULL;
			hitCount++;

			if (hitCount >= Config.get().MODEL_PREDICTION_PREDICATE_HIT_COUNT) {
				break;
			}
		}
		
		if (hitCount == 0) {
			return null;
		}
		
		rewardAccumulator /= hitCount;
		
		return rewardAccumulator;
	}
	
	public Boolean isHit(HashMap<IFeature, Integer> inputs, int artifactIndex) {
		Predicate predicate = this.predicates.get(artifactIndex);
		ArrayList<Predicate.Condition> parcels = predicate.opHits;
		
		boolean hit = true;
		
		for (Predicate.Condition parcel : parcels) {
			IFeature thd = parcel.operator;
			Integer parcelOutcome = parcel.hit;
			
			try {
				int thdOutcome = inputs.get(thd);
				
				if (parcelOutcome.intValue() != thdOutcome) {
					hit = false;
					break;
				}
			}
			catch (Exception e) {
				hit = false;
				e.printStackTrace();
			}
		}
		
		return hit;
	}

	public boolean isEmpty() {
		return this.getPredicates().size() == 0;
	}

	public IFeature getRewardSelector() {
		return rewardSelector;
	}

	public ArrayList<IFeature> getInputs() {
		return this.dataset.getFunctors();
	}
	
	public ArrayList<Predicate> getPredicates() {
		return this.predicates;
	}

	public InnerDataset getDataset() {
		return this.dataset;
	}
	
	private HashMap<IFeature, Integer> getInputsByTime(int time) {
		HashMap<IFeature, Integer> inputs = new HashMap<IFeature, Integer>();
		for (IFeature param : this.dataset.getFunctors()) {
			int val = this.dataset.getValueByTime(param, time);
			inputs.put(param, val);
		}
		return inputs;
	}

	public void setArtifacts(ArrayList<Predicate> predicates) {
		this.predicates = predicates;
	}
}
