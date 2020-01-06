package plugins.openml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import unn.dataset.DatasetLocator;
import unn.dataset.OuterDataset;
import unn.dataset.OuterDatasetLoader;
import unn.interfaces.IOperator;
import unn.mining.Artifact;
import unn.mining.MiningStatusObservable;
import unn.mining.Model;
import unn.mining.StatsWalker;
import unn.morphing.Morpher;
import unn.structures.Context;
import unn.structures.MiningStatus;

public class EnvironmentGroup {
	private int datasetId;
	private HashMap<String, MiningEnvironment> envs;
	private JobConfig config;
	private Context context;
	private OuterDataset outerDataset;

	public EnvironmentGroup(Context context, int datasetId) {
		this.envs = new HashMap<String, MiningEnvironment>();
		this.datasetId = datasetId;
		this.context = context;
	}
	
	public void load(DatasetLocator locator) {		
		try {
			OuterDatasetLoader loader = new OuterDatasetLoader();
			this.outerDataset = loader.load(locator);
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mine(JobConfig config) {
		this.config = config;
		
		MiningEnvironment seedEnv = new MiningEnvironment(this.outerDataset);
		seedEnv.init(this.context, config);

		UnitReport units = seedEnv.getUnitReport();
		OuterValueType vType = units.getValues(config.targetFeature);

		if (vType instanceof DiscreteSet) {
			DiscreteSet set = (DiscreteSet) vType;

			for (String value : set.values) {
				MiningEnvironment env = new MiningEnvironment(this.outerDataset);
				envs.put(value, env);
			}
			
			for (String value : set.values) {
				try {
					MiningEnvironment env = envs.get(value);
					JobConfig newConfig = (JobConfig) config.clone();
					newConfig.setTargetOuterValue(value);
					this.context.registerJobConfig(newConfig);
					env.init(this.context, newConfig);
					env.mine();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (vType instanceof NumericMapper) {
			NumericMapper numericMapper = (NumericMapper) vType;
			ArrayList<Integer> innerValues = numericMapper.getAllInnerValues();

			for (Integer innerValue : innerValues) {
				MiningEnvironment env = new MiningEnvironment(this.outerDataset);
				envs.put(Integer.toString(innerValue), env);
			}
			
			for (Integer innerValue : innerValues) {
				try {
					MiningEnvironment env = envs.get(Integer.toString(innerValue));
					JobConfig newConfig = (JobConfig) config.clone();
					newConfig.setTargetInnerValue(innerValue);
					this.context.registerJobConfig(newConfig);
					env.init(this.context, newConfig);
					env.mine();
				} 
				catch (Exception e) {
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
		
		UnitReport units = from.getUnitReport();
		//UnitReport unitsTo = to.getUnitReport();
		
		//OuterValueType typeFrom = unitsFrom.getValues("\"type\"");
		//OuterValueType typeTo = unitsTo.getValues("\"type\"");
		
		Morpher morpher = new Morpher();
		morpher.init(from.getModel(), units, to.getModel());
		
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

	public OuterDataset getOuterDataset() {
		return outerDataset;
	}
}
