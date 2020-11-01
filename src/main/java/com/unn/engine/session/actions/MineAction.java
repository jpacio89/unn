package com.unn.engine.session.actions;

import com.unn.engine.Config;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.models.MiningScope;
import com.unn.engine.mining.models.Model;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

		for (String group : valuesDescriptor.getGroups()) {
			IFunctor op = valuesDescriptor.getFunctorByGroup(group);
			targetGroups.add(op);
		}

		Pair<ArrayList<Integer>, ArrayList<Integer>> splittedTimes = splitDataset(innerDataset);
		this.session.setMakerTimes(splittedTimes.first());

		for (IFunctor func : targetGroups) {
			ScopeConfig scopeConf = new ScopeConfig(
				loader,
				innerDataset,
				config.targetFeature,
				func,
				targetGroups,
				splittedTimes.first(),
				splittedTimes.second());
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

	private Pair<ArrayList<Integer>, ArrayList<Integer>> splitDataset(InnerDataset dataset) {
		ArrayList<Integer> allTimes = dataset.getTimes().stream()
			.collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(allTimes);
		int midPoint = allTimes.size() / 2;
		ArrayList<Integer> trainTimeSets = allTimes.stream()
				.limit(midPoint)
				.collect(Collectors.toCollection(ArrayList::new));
		ArrayList<Integer> testTimeSets = allTimes.stream()
				.skip(midPoint)
				.collect(Collectors.toCollection(ArrayList::new));
		return new Pair<>(testTimeSets, testTimeSets);
	}
}
