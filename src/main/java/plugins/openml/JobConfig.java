package plugins.openml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import unn.structures.Config;


public class JobConfig {
	public final static JobConfig DEFAULT = new JobConfig("", new LinkedList<String>());
	
	public String targetFeature;
	public String targetOuterValue;
	public Integer targetInnerValue;
	public String[] featureBlacklist;
	public HashMap<String, Integer> groupCount;
	
	//public static Integer mapReward(Integer val) {
	//	return val != null && val == 5 ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE;
		
	public JobConfig() {}
	
	public JobConfig(String targetFeature, List<String> featureBlacklist) {
		this.targetFeature = targetFeature;
		this.featureBlacklist = featureBlacklist.toArray(new String[featureBlacklist.size()]);
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

}
