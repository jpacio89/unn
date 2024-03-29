package com.unn.engine.session.actions;

import com.unn.engine.dataset.BoosterProvider;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.mining.splitters.HashSplitter;
import com.unn.engine.mining.splitters.SimpleSplitter;
import com.unn.engine.session.Session;
import com.unn.engine.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

		if (dataset.getHeader().size() <= 3 ||
			dataset.sampleCount() == 0) {
			return;
		}

		HashMap<String, MiningScope> scopes = this.session.getScopes();
		JobConfig config = getConf();

		InnerDatasetLoader loader = new InnerDatasetLoader();
		loader.init(dataset);
		this.session.setInnerDatasetLoader(loader);
		InnerDataset innerDataset = loader.load();
		ValueMapper mapper = loader.getValueMapper();
		ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(
				config.targetFeature);

		ArrayList<IFeature> targetGroups = new ArrayList<>();

		for (String group : valuesDescriptor.getOutputFeatures()) {
			IFeature op = valuesDescriptor.getFeatureByName(group);
			targetGroups.add(op);
		}

		// NOTE: if use Dataset boosting
		// TODO: uncomment when this module has been tested
		//BoosterProvider boosterProvider = new BoosterProvider(innerDataset);
		//innerDataset = boosterProvider.boost(targetGroups);

		Pair<ArrayList<Integer>, ArrayList<Integer>> splittedTimes = splitDataset(innerDataset);
		this.session.setMakerTimes(splittedTimes.first());

		for (IFeature func : targetGroups) {
			ScopeConfig scopeConf = new ScopeConfig(
				config.targetFeature,
				func,
				targetGroups,
				splittedTimes.first(),
				splittedTimes.second());
			MiningScope scope = new MiningScope(scopeConf);
			String scopeName = func.getName();
			scopes.put(scopeName, scope);
		}

		// NOTE: for each scope mine the dataset and check model
		//		 if model is empty, remove the scope from list
		ArrayList<String> idsToRemove = scopes.entrySet().stream()
			.map(entry -> {
				String id = entry.getKey();
				MiningScope scope = entry.getValue();
				try {
					scope.mine(this.session);
					if (scope.getModel() == null ||
						scope.getModel().isEmpty()) {
						return id;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return id;
				}
				return null;
			})
			.filter(id -> id != null)
			.collect(Collectors.toCollection(ArrayList::new));

		for (String id : idsToRemove) {
			scopes.remove(id);
		}

		System.out.println("|MineActor| All scopes have been processed");
	}

	private Pair<ArrayList<Integer>, ArrayList<Integer>> splitDataset(InnerDataset dataset) {
		HashSplitter splitter = new HashSplitter(this.conf.layer);
		//SimpleSplitter splitter = new SimpleSplitter(0.75);
		return splitter.split(dataset.getTimes());
	}

}
