package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.utils.Pair;
import com.unn.engine.utils.Triplet;

public class PreviousState {
	HashMap<Integer, Triplet<Long, Double, Long>> summaries;
	HashMap<Integer, Pair<ArrayList<Long>, Pair<Double, Long>>> history;
	Long[] weights;
	Double errorSum;
	
	public PreviousState() {
		this.history = new HashMap<Integer, Pair<ArrayList<Long>, Pair<Double, Long>>>();
		this.summaries = new HashMap<Integer, Triplet<Long, Double, Long>>();
	}
	
	public void setSummary(Integer time, Triplet<Long, Double, Long> summary) {
		this.summaries.put(time, summary);
	}
	
	public Triplet<Long, Double, Long> getSummaries(Integer time) {
		return this.summaries.get(time);
	}
	
	public void setPreviousWeights(Long[] previousWeights) {
		this.weights = previousWeights;
	}
	
	public Long[] getPreviousWeights(int time) {
		return weights;
	}
	
	public void setHitWeights(int time, Pair<ArrayList<Long>, Pair<Double, Long>> hitWeights) {
		this.history.put(time, hitWeights);
	}

	public Pair<ArrayList<Long>, Pair<Double, Long>> getHitWeights(Integer time) {
		return this.history.get(time);
	}

	public void setError(double errorSum) {
		this.errorSum = errorSum;
	}
	
	public double getError() {
		return this.errorSum;
	}
}