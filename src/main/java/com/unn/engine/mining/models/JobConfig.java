package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.*;

import com.unn.engine.Config;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.Triplet;


public class JobConfig implements Serializable {
	private static final long serialVersionUID = 3698978410470063835L;
	public final static JobConfig DEFAULT = new JobConfig("", new LinkedList<>());
	
	public String jobSessionId;
	public String targetFeature;
	public String targetOuterValue;
	public Integer targetInnerValue;
	public String[] featureBlacklist;
	public String timeFeatureName;
	public String rewardFeatureName;
		
	public JobConfig() {
		this.generateId();
	}
	
	public JobConfig(String targetFeature, List<String> featureBlacklist) {
		this.targetFeature = targetFeature;
		this.featureBlacklist = featureBlacklist.toArray(new String[featureBlacklist.size()]);
		this.generateId();
	}
	
	public void setTargetOuterValue(String val) {
		this.targetOuterValue = val;
	}
	
	public void setTargetInnerValue(Integer innerValue) {
		this.targetInnerValue = innerValue;
	}
	
	public static Integer mapReward(Integer ref, Integer val) {
		return val != null && val == ref ? Config.get().STIM_MAX : Config.get().STIM_MIN;
	}

	// TODO: call this method in environment group
	public void generateId() {
		this.jobSessionId = UUID.randomUUID().toString();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		JobConfig newConf = new JobConfig();
		newConf.targetFeature = this.targetFeature;
		newConf.targetOuterValue = this.targetOuterValue;
		newConf.targetInnerValue = this.targetInnerValue;
		
		if (this.featureBlacklist != null) {
			newConf.featureBlacklist = Arrays.copyOf(this.featureBlacklist, this.featureBlacklist.length);
		}
		
		return newConf;
	}


	public String getTimeFeatureName() {
		return timeFeatureName;
	}

	public void setTimeFeatureName(String timeFeatureName) {
		this.timeFeatureName = timeFeatureName;
	}

	public String getRewardFeatureName() {
		return rewardFeatureName;
	}

	public void setRewardFeatureName(String rewardFeatureName) {
		this.rewardFeatureName = rewardFeatureName;
	}

	public static class PreviousState {
		public HashMap<Integer, Triplet<Long, Double, Long>> summaries;
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
}
