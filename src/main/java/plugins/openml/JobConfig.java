package plugins.openml;

import java.util.LinkedList;
import java.util.List;


public class JobConfig {
	public static JobConfig DEFAULT = new JobConfig("", new LinkedList<String>());
	
	public String targetFeature;
	// public String classValue = "fish";
	public String[] featureBlacklist;
	//public static Integer mapReward(Integer val) {
	//	return val != null && val == 5 ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE;
		
	public JobConfig() {}
	
	public JobConfig(String targetFeature, List<String> featureBlacklist) {
		this.targetFeature = targetFeature;
		this.featureBlacklist = featureBlacklist.toArray(new String[featureBlacklist.size()]);
	}
	
	
	
	
}
