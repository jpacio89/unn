package com.unn.engine.session.actors;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.*;
import com.unn.engine.mining.ScopeConfig;
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
		HashMap<String, MiningScope> scopes = this.session.getScopes();
		JobConfig config = buildConfig();

		InnerDatasetLoader loader = new InnerDatasetLoader();
		loader.init(context, config, dataset);
		ValueMapper mapper = loader.getValueMapper();
		InnerDataset innerDataset = loader.load();
		ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(
			config.targetFeature);
		ArrayList<IFunctor> rewardGroups = new ArrayList<>();
		for (String group : valuesDescriptor.getGroups()) {
			IFunctor op = valuesDescriptor.getFunctorByGroup(group);
			rewardGroups.add(op);
		}
		for (IFunctor func : rewardGroups) {
			ScopeConfig scopeConf = new ScopeConfig(loader, innerDataset, func, rewardGroups);
			MiningScope scope = new MiningScope(scopeConf);
			scopes.put(func.getDescriptor().getClassId(), scope);
		}
		for (MiningScope scope : scopes.values()) {
			try {
				scope.mine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	private JobConfig buildConfig() {
		return this.action.getConf();
	}
}
