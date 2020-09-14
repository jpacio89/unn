package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.Config;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.session.Context;

public class MiningScope implements IEnvironment, Serializable {
	private static final long serialVersionUID = -8783414205445675354L;
	private ValueMapper mapper;
	private Model refinedModel;
	private Context context;
	private JobConfig config;
	private OuterDataset outerDataset;
	private InnerDataset innerDataset;
	
	public MiningScope(OuterDataset outerDataset) {
		this.outerDataset = outerDataset;
	}
	
	@Override
	public ArrayList<IOperator> getInputs(String market) {
		if (this.refinedModel == null) {
			return null;
		}
		return this.refinedModel.getInputs();
	}
	
	private MiningStatusObservable getStatusObservable() {
		if (this.context == null) {
			return new MiningStatusObservable();
		}
		MiningStatusObservable obs = this.context.getStatusObservable(this.config.jobSessionId);
		if (obs != null) {
			return obs;
		}
		return new MiningStatusObservable();
	}
	
	public void init(Context context, JobConfig config) {
		System.out.println(String.format("|MiningEnvironment| Initializing miner"));
		
		this.config = config;
		this.context = context;
		getStatusObservable().updateStatusLabel("LOADING");
		
		InnerDatasetLoader loader = new InnerDatasetLoader();
		loader.init(this.context, this.config, this.outerDataset);
		this.innerDataset = loader.load();
		this.mapper = loader.getValueMapper();
	}
	
	public StatsWalker mine() throws Exception {
		System.out.println(String.format("|MiningEnvironment| Mining"));
		this.refinedModel = null;
		
		getStatusObservable().updateStatusLabel("BUFFERING");
		
		Miner miner = new Miner(this.innerDataset, getStatusObservable());
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
		//this.refinedModel = model;
		
		int countMin = this.innerDataset.count(Config.STIMULI_MIN_VALUE);
		int countNull = this.innerDataset.count(Config.STIMULI_NULL_VALUE);
		int countMax = this.innerDataset.count(Config.STIMULI_MAX_VALUE);
		
		System.out.println("Min Count = " + countMin);
		System.out.println("Null Count = " + countNull);
		System.out.println("Max Count = " + countMax);
		
		this.innerDataset.shrink();
		
		getStatusObservable().updateStatusLabel("DONE");
		
		return this.refinedModel.getStatsWalker();
	}
	
	public StatsWalker getStatsWalker() {
		if (this.refinedModel != null) {
			return this.refinedModel.getStatsWalker();
		}
		return null;
	}
	
	public ValueMapper getMapper() {
		return mapper;
	}
	
	public Double predict(HashMap<IOperator, Integer> inputs) {
		if (this.refinedModel == null) {
			return null;
		}
		return this.refinedModel.predict(inputs, null, null);
	}
	
	public Model getModel() {
		return this.refinedModel;
	}

	public JobConfig getConfig() {
		return config;
	}

	public void setConfig(JobConfig config) {
		this.config = config;
	}
}
