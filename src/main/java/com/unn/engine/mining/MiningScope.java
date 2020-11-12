package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.Config;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.Miner;
import com.unn.engine.mining.StatisticsAnalyzer;
import com.unn.engine.mining.RefineryNew;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.mining.models.Model;
import com.unn.engine.mining.models.ScopeConfig;

public class MiningScope implements IEnvironment, Serializable {
	private Model refinedModel;
	private ScopeConfig config;
	
	public MiningScope(ScopeConfig config) {
		this.config = config;
	}
	
	@Override
	public ArrayList<IFunctor> getInputs(String market) {
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
	
	public StatisticsAnalyzer mine() throws Exception {
		System.out.println(String.format("|MiningEnvironment| Mining"));
		this.refinedModel = null;
		
		getStatusObservable().updateStatusLabel("BUFFERING");

		Miner miner = new Miner (
			getInnerDataset(),
			this.config.getInnerFeature(),
			this.config.getNoMiningGroups(),
			getStatusObservable()
		);
		miner.init(config.getTrainTimes(), config.getTestTimes());
		
		if (!miner.ready()) {
			System.out.println(String.format(" Not enough data. Skipping..."));
			getStatusObservable().updateStatusLabel("DONE");
			return null;
		}
		
		miner.mine();
		
		getStatusObservable().updateStatusLabel("OPTIMIZING");
		
		Model model = miner.getModel();
		Refinery refinery = new Refinery(miner, model);
		
		if (Config.ASSERT) {
			// refinery.checkConsistency();
		}

		System.out.println(String.format("|MiningScope| Gross artifacts produced: %d.", model.getArtifacts().size()));

		if (model.getArtifacts().size() > 0) {
			this.refinedModel = refinery.refine();
			//this.refinedModel = model;
		} else {
            this.refinedModel = model;
			System.out.println("|MiningScope| Wheats are not separable from Weeds -> dataset in equilibrium for this scope.");
		}

		System.out.println(String.format("|MiningScope| Refined artifacts produced: %d.", this.refinedModel.getArtifacts().size()));
		getStatusObservable().updateStatusLabel("DONE");
		
		return this.refinedModel.getStatsWalker();
	}
	
	public StatisticsAnalyzer getStatisticsAnalyzer() {
		if (this.refinedModel != null) {
			return this.refinedModel.getStatsWalker();
		}
		return null;
	}
	
	public Double predict(HashMap<IFunctor, Integer> inputs) {
		if (this.refinedModel == null) {
			return null;
		}
		Double prediction = this.refinedModel.predict(inputs, null, null);
		if (prediction == null) {
			return 0.0;
		}
		return prediction;
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
