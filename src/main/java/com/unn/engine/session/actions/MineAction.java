package com.unn.engine.session.actions;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.models.MiningScope;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MineAction extends Action {
	Session session;
	JobConfig conf;
	
	public MineAction() { }

	public JobConfig getConf() {
		return conf;
	}

	public void setConf(JobConfig conf) {
		this.conf = conf;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void act() {
		OuterDataset dataset = this.session.getOuterDataset();
		Context context = this.session.getContext();
		HashMap<String, MiningScope> scopes = this.session.getScopes();
		JobConfig config = getConf();

		InnerDatasetLoader loader = new InnerDatasetLoader();
		loader.init(context, config, dataset);
		InnerDataset innerDataset = loader.load();
		ValueMapper mapper = loader.getValueMapper();
		ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(
				config.targetFeature);

		ArrayList<IFunctor> targetGroups = new ArrayList<>();

		for (String group : valuesDescriptor.getGroups(config.targetFeature)) {
			IFunctor op = valuesDescriptor.getFunctorByGroup(group);
			targetGroups.add(op);
		}

		for (IFunctor func : targetGroups) {
			ScopeConfig scopeConf = new ScopeConfig(loader, innerDataset,
					config.targetFeature, func, targetGroups);
			MiningScope scope = new MiningScope(scopeConf);
			String scopeName = func.getDescriptor().getVtrName();
			scopes.put(scopeName, scope);
		}

		ArrayList<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, MiningScope> entry : scopes.entrySet()) {
			String id = entry.getKey();
			MiningScope scope = entry.getValue();
			try {
				scope.mine();
				if (scope.getModel().isEmpty()) {
					toRemove.add(id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		toRemove.forEach(scopeId -> scopes.remove(scopeId));
		System.out.println("|MineActor| All scopes have been processed");
	}
}
