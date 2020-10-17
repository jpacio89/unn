package com.unn.engine.mining;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.unn.engine.Config;


public class JobConfig implements Serializable {
	private static final long serialVersionUID = 3698978410470063835L;
	public final static JobConfig DEFAULT = new JobConfig("", new LinkedList<String>());
	
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
		return val != null && val == ref ? Config.STIM_MAX : Config.STIM_MIN;
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
}
