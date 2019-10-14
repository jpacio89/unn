package plugins.openml;

import unn.Config;

public class DatasetConfig {
	public static String className = "\"type\"";
	public static String classValue = "fish";
	public static String[] featureBlacklist = { "\"animal\"", "\"legs\"" };
	public static Integer mapReward(Integer val) {
		return val != null && val == 5 ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE;
	}
}
