package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.mining.models.Model;
import com.unn.engine.mining.models.ScopeConfig;

public class MiningScope implements IEnvironment, Serializable {
	private Model refinedModel;
	private ScopeConfig config;

	private Miner miner;
	
	public MiningScope(ScopeConfig config) {
		this.config = config;
	}
	
	@Override
	public ArrayList<IFeature> getInputs(String market) {
		if (this.refinedModel == null) {
			return null;
		}
		return this.refinedModel.getInputs();
	}
	
	private MiningStatusObservable getStatusObservable() {
		/*if (this.context == null) {
			return new MiningStatusObservable();
		}
		MiningStatusObservable obs = this.context.getStatusObservable(this.config.jobSessionId);
		if (obs != null) {
			return obs;
		}*/
		return new MiningStatusObservable();
	}

	private InnerDataset getInnerDataset() {
		return this.config.getInnerDataset();
	}

	public ValueMapper getMapper() {
		return this.config.getLoader().getValueMapper();
	}
	
	public PerformanceAnalyzer mine() throws Exception {
		System.out.println(String.format("|MiningEnvironment| Mining"));
		this.refinedModel = null;
		
		getStatusObservable().updateStatusLabel("BUFFERING");

		this.miner = new Miner (
			getInnerDataset(),
			this.config.getInnerFeature(),
			this.config.getNoMiningGroups(),
			getStatusObservable()
		);

		this.miner.init(config.getTrainTimes());
		
		if (!this.miner.ready()) {
			System.out.println(String.format(" Not enough data. Skipping..."));
			getStatusObservable().updateStatusLabel("DONE");
			return null;
		}
		
		this.miner.mine();
		
		getStatusObservable().updateStatusLabel("OPTIMIZING");
		
		Model model = this.miner.getModel();
		model.calculatePerformance(config.getTestTimes());

		System.out.println(String.format("|MiningScope| Gross artifacts produced: %d.", model.getPredicates().size()));

		this.refinedModel = model;

		if (model.getPredicates().size() == 0) {
			System.out.println("|MiningScope| wheat events are not separable from weeds -> dataset in equilibrium for this scope.");
		}

		System.out.println(String.format("|MiningScope| Refined artifacts produced: %d.", this.refinedModel.getPredicates().size()));
		getStatusObservable().updateStatusLabel("DONE");
		
		return this.refinedModel.getStatsWalker();
	}
	
	public PerformanceAnalyzer getStatisticsAnalyzer() {
		if (this.refinedModel != null) {
			return this.refinedModel.getStatsWalker();
		}
		return null;
	}
	
	public Double predict(HashMap<IFeature, Integer> inputs) {
		if (this.refinedModel == null) {
			return null;
		}
		Double prediction = this.refinedModel.predict(inputs);
		if (prediction == null) {
			return 0.0;
		}
		return prediction;
	}

	public Miner getMiner() {
		return miner;
	}
	
	public Model getModel() {
		return this.refinedModel;
	}

	public ScopeConfig getConfig() {
		return config;
	}

	public void setConfig(ScopeConfig config) {
		this.config = config;
	}
}
