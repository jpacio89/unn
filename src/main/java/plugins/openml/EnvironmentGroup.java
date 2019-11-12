package plugins.openml;

import java.util.HashMap;

import unn.mining.StatsWalker;

public class EnvironmentGroup {
	private int datasetId;
	private HashMap<String, MiningEnvironment> envs;
	private JobConfig config;
	
	public EnvironmentGroup(int datasetId) {
		this.envs = new HashMap<String, MiningEnvironment>();
		this.datasetId = datasetId;
	}

	public void mine(JobConfig config) {
		this.config = config;
		
		MiningEnvironment seedEnv = new MiningEnvironment(this.datasetId);
		seedEnv.init(config);
		
		UnitReport units = seedEnv.getUnitReport();
		OuterValueType vType = units.getValues(config.targetFeature);
		
		if (vType instanceof DiscreteSet) {
			DiscreteSet set = (DiscreteSet) vType;
		
			for (String value : set.values) {
				MiningEnvironment env = new MiningEnvironment(this.datasetId);
				
				config.setTargetOuterValue(value);
				envs.put(value, env);
				
				env.init(config);
				
				try {
					env.mine();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public JobConfig getConfig() {
		return this.config;
	}
	
	public MiningReport getReport() {
		MiningReport report = new MiningReport();
		
		for (String value : envs.keySet()) {
			MiningEnvironment env = envs.get(value);
			StatsWalker stats = env.getStatsWalker();
			report.confusionMatrixes.put(value, stats);
		}
	
		return report;
	}
	
	public HashMap<String, MiningEnvironment> getEnvironments() {
		return this.envs;
	}
}
