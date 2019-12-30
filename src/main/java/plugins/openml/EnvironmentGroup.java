package plugins.openml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import unn.interfaces.IOperator;
import unn.mining.Artifact;
import unn.mining.MiningStatusObservable;
import unn.mining.Model;
import unn.mining.StatsWalker;
import unn.morphing.Morpher;
import unn.structures.MiningStatus;

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
				envs.put(value, env);
			}
			
			for (String value : set.values) {
				MiningEnvironment env = envs.get(value);
				config.setTargetOuterValue(value);

				env.init(config);

				try {
					env.mine();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (vType instanceof NumericMapper) {
			NumericMapper numericMapper = (NumericMapper) vType;
			ArrayList<Integer> innerValues = numericMapper.getAllInnerValues();

			for (Integer innerValue : innerValues) {
				MiningEnvironment env = new MiningEnvironment(this.datasetId);
				envs.put(Integer.toString(innerValue), env);
			}
			
			for (Integer innerValue : innerValues) {
				MiningEnvironment env = envs.get(Integer.toString(innerValue));
				config.setTargetInnerValue(innerValue);	

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
			Model model = env.getModel();
			report.confusionMatrixes.put(value, stats);
			
			ArrayList<String> artifactSigs = new ArrayList<String>();
			
			if (model != null && model.getArtifacts() != null) {
				for (Artifact fact : model.getArtifacts()) {
					artifactSigs.add(fact.toString());
				}
				
				Collections.sort(artifactSigs);				
			}

			report.artifactSignatures.put(value, artifactSigs);
		}

		return report;
	}
	
	// TODO: for now values passed in are INNER values
	public HashMap<IOperator, Integer> morph(HashMap<IOperator, Integer> inputs, String classValueFrom, String classValueTo) {
		/*MiningEnvironment seedEnv = new MiningEnvironment(this.datasetId);
		seedEnv.init(config);

		UnitReport units = seedEnv.getUnitReport();
		
		OuterValueType vType = units.getValues(config.targetFeature);*/
		MiningEnvironment from = this.envs.get(classValueFrom);
		MiningEnvironment to = this.envs.get(classValueTo);
		
		UnitReport unitsFrom = from.getUnitReport();
		UnitReport unitsTo = to.getUnitReport();
		
		OuterValueType typeFrom = unitsFrom.getValues(classValueFrom);
		OuterValueType typeTo = unitsTo.getValues(classValueTo);
		
		Morpher morpher = new Morpher();
		morpher.init(from.getModel(), typeFrom, to.getModel(), typeTo);
		
		HashMap<IOperator, Integer> ret = morpher.morph(inputs);
		return ret;
	}

	public HashMap<String, MiningEnvironment> getEnvironments() {
		return this.envs;
	}
	
	public HashMap<String, UnitReport> getUnitReports() {
		HashMap<String, UnitReport> reports = new HashMap<String, UnitReport>();
		
		for (Entry<String, MiningEnvironment> entry : this.envs.entrySet()) {
			reports.put(entry.getKey(), entry.getValue().getUnitReport());
		}
		
		return reports;
	}
	
	public HashMap<String, MiningStatus> getMiningStatuses() {
		HashMap<String, MiningStatus> statuses = new HashMap<String, MiningStatus>();
		
		for (Entry<String, MiningEnvironment> entry : this.envs.entrySet()) {
			statuses.put(entry.getKey(), entry.getValue().getMiningStatus());
		}
		
		return statuses;
	}
}
