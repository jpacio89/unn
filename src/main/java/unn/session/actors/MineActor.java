package unn.session.actors;

import java.util.ArrayList;
import java.util.HashMap;

import plugins.openml.DiscreteSet;
import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import plugins.openml.NumericMapper;
import plugins.openml.OuterValueType;
import plugins.openml.UnitReport;
import unn.dataset.OuterDataset;
import unn.session.Session;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.MineAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveModelAction;
import unn.structures.Context;

public class MineActor extends Actor {
	Session session;
	MineAction action;
	
	public MineActor(Session session, MineAction action) {
		this.session = session;
		this.action = action;
	}

	public ActionResult write() {
		OuterDataset dataset = this.session.getOuterDataset();
		Context context = this.session.getContext();
		
		// TODO: remove this from Session
		HashMap<String, MiningEnvironment> envs = this.session.getEnvs();
		JobConfig config = buildConfig();
		
		MiningEnvironment seedEnv = new MiningEnvironment(dataset);
		seedEnv.init(context, config);

		UnitReport units = seedEnv.getUnitReport();
		OuterValueType vType = units.getValues(config.targetFeature);

		if (vType instanceof DiscreteSet) {
			DiscreteSet set = (DiscreteSet) vType;

			for (String value : set.values) {
				MiningEnvironment env = new MiningEnvironment(dataset);
				envs.put(value, env);
			}
			
			for (String value : set.values) {
				try {
					MiningEnvironment env = envs.get(value);
					JobConfig newConfig = (JobConfig) config.clone();
					newConfig.setTargetOuterValue(value);
					context.registerJobConfig(newConfig);
					env.init(context, newConfig);
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
				MiningEnvironment env = new MiningEnvironment(dataset);
				envs.put(Integer.toString(innerValue), env);
			}
			
			for (Integer innerValue : innerValues) {
				try {
					MiningEnvironment env = envs.get(Integer.toString(innerValue));
					JobConfig newConfig = (JobConfig) config.clone();
					newConfig.setTargetInnerValue(innerValue);
					
					context.registerJobConfig(newConfig);
					
					env.init(context, newConfig);
					env.mine();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// TODO: fix this
		return null;
	}
	
	private JobConfig buildConfig() {
		return this.action.getConf();
	}
}
