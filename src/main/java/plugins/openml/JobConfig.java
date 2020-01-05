package plugins.openml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import unn.structures.Config;


public class JobConfig {
	public final static JobConfig DEFAULT = new JobConfig("", new LinkedList<String>());
	
	public String jobSessionId;
	public String targetFeature;
	public String targetOuterValue;
	public Integer targetInnerValue;
	public String[] featureBlacklist;
	public HashMap<String, Integer> groupCount;
	
	//public static Integer mapReward(Integer val) {
	//	return val != null && val == 5 ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE;
		
	public JobConfig() {
		this.groupCount = new HashMap<String, Integer>();
		this.generateId();
	}
	
	public JobConfig(String targetFeature, List<String> featureBlacklist) {
		this.groupCount = new HashMap<String, Integer>();
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
		return val != null && val == ref ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE;
	}

	// TODO: call this method in environment group
	public void generateId() {
		this.jobSessionId = UUID.randomUUID().toString();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		JobConfig newConf = new JobConfig();
		newConf.targetFeature = this.targetFeature;
		newConf.targetOuterValue = this.targetOuterValue;
		newConf.targetInnerValue = this.targetInnerValue;
		
		if (this.featureBlacklist != null) {
			newConf.featureBlacklist = Arrays.copyOf(this.featureBlacklist, this.featureBlacklist.length);
		}
		
		newConf.groupCount = new HashMap<String, Integer>();
		newConf.groupCount.putAll(this.groupCount);
		
		return newConf;
	}
	
	
}
