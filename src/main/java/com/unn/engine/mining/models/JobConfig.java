package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.*;

import com.unn.engine.Config;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.Triplet;


public class JobConfig implements Serializable {
	private static final long serialVersionUID = 3698978410470063835L;

	public String jobSessionId;
	public String targetFeature;
	public String targetOuterValue;
	public Integer targetInnerValue;
	public String[] featureBlacklist;
	public int layer;

	public JobConfig() {
		this.generateId();
	}
	
	public JobConfig(String targetFeature, List<String> featureBlacklist, int layer) {
		this.targetFeature = targetFeature;
		this.featureBlacklist = featureBlacklist.toArray(new String[featureBlacklist.size()]);
		this.layer = layer;
		this.generateId();
	}

	public JobConfig(String targetFeature, List<String> featureBlacklist) {
		this.targetFeature = targetFeature;
		this.featureBlacklist = featureBlacklist.toArray(new String[featureBlacklist.size()]);
		this.layer = 1;
		this.generateId();
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

}
