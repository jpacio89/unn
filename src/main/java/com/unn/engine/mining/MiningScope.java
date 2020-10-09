package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.Config;
import com.unn.engine.metadata.ValueMapper;

public class MiningScope implements IEnvironment, Serializable {
	private static final long serialVersionUID = -8783414205445675354L;
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
	
	public StatsWalker mine() throws Exception {
		System.out.println(String.format("|MiningEnvironment| Mining"));
		this.refinedModel = null;
		
		getStatusObservable().updateStatusLabel("BUFFERING");

		Miner miner = new Miner (
			getInnerDataset(),
			this.config.getInnerFeature(),
			this.config.getNoMiningGroups(),
			getStatusObservable()
		);
		miner.init();
		
		if (!miner.ready()) {
			System.out.println(String.format(" Not enough data. Skipping..."));
			getStatusObservable().updateStatusLabel("DONE");
			return null;
		}
		
		miner.mine();
		
		getStatusObservable().updateStatusLabel("OPTIMIZING");
		
		Model model = miner.getModel();
		RefineryNew refinery = new RefineryNew(miner, model);
		
		if (Config.ASSERT) {
			// refinery.checkConsistency();
		}
		
		this.refinedModel = refinery.refine();
		
		getStatusObservable().updateStatusLabel("DONE");
		
		return this.refinedModel.getStatsWalker();
	}
	
	public StatsWalker getStatsWalker() {
		if (this.refinedModel != null) {
			return this.refinedModel.getStatsWalker();
		}
		return null;
	}
	
	public Double predict(HashMap<IFunctor, Integer> inputs) {
		if (this.refinedModel == null) {
			return null;
		}
		return this.refinedModel.predict(inputs, null, null);
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
