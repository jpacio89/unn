package com.unn.engine.session.actors;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.metadata.*;
import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.MineAction;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.session.Session;
import com.unn.engine.session.Context;

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
		HashMap<String, MiningScope> envs = this.session.getScopes();
		JobConfig config = buildConfig();
		
		MiningScope seedEnv = new MiningScope(dataset);
		seedEnv.init(context, config);

		ValueMapper units = seedEnv.getMapper();
		OuterValueType vType = units.getValues(config.targetFeature);

		if (vType instanceof DiscreteSet) {
			DiscreteSet set = (DiscreteSet) vType;

			for (String value : set.values) {
				MiningScope env = new MiningScope(dataset);
				envs.put(value, env);
			}
			
			for (String value : set.values) {
				try {
					MiningScope env = envs.get(value);
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
				MiningScope env = new MiningScope(dataset);
				envs.put(Integer.toString(innerValue), env);
			}
			
			for (Integer innerValue : innerValues) {
				try {
					MiningScope env = envs.get(Integer.toString(innerValue));
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
