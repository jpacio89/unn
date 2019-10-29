package plugins.openml;

import java.util.LinkedList;
import java.util.List;

import unn.Config;


public class JobConfig {
	public static JobConfig DEFAULT = new JobConfig("", new LinkedList<String>());
	
	public String targetFeature;
	public String targetOuterValue;
	public String[] featureBlacklist;
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
	
	public static Integer mapReward(Integer ref, Integer val) {
		return val != null && val == ref ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE;
	 }

}
