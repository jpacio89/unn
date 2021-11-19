package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.mining.models.Model;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.session.Session;

public class MiningScope implements IEnvironment, Serializable {
	private ScopeConfig config;
	private transient Miner miner;
	private Model model;
	
	public MiningScope(ScopeConfig config) {
		this.config = config;
	}
	
	@Override
	public ArrayList<IFeature> getInputs(String market) {
		if (this.model == null) {
			return null;
		}
		return this.model.getInputs();
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
	
	public void mine(Session session) throws Exception {
		getStatusObservable().updateStatusLabel("BUFFERING");

		System.out.println(String.format("|MiningEnvironment| Mining"));

		this.model = null;
		this.miner = new Miner (
			session.getInnerDatasetLoader().getInitialInnerDataset(),
			this.config.getInnerFeature(),
			this.config.getNoMiningGroups(),
			getStatusObservable()
		);
		this.miner.init(config.getTrainTimes());

		if (!this.miner.ready()) {
			System.out.println(String.format("|MiningScope| Miner failed to initialize"));
			getStatusObservable().updateStatusLabel("DONE");
			return;
		}

		getStatusObservable().updateStatusLabel("MINING");

		this.miner.mine();

		getStatusObservable().updateStatusLabel("OPTIMIZING");

		this.model = this.miner.getModel();
		this.model.calculatePerformance(this.config.getTestTimes());

		if (this.model.getPredicates().size() == 0) {
			System.out.println("|MiningScope| Events are not separable: dataset in equilibrium");
		}

		System.out.println(String.format("|MiningScope| Predicates produced: %d.", this.model.getPredicates().size()));
		getStatusObservable().updateStatusLabel("DONE");
	}

	public Miner getMiner() {
		return this.miner;
	}

	public Model getModel() {
		return this.model;
	}

	public ScopeConfig getConfig() {
		return this.config;
	}

	public void setConfig(ScopeConfig config) {
		this.config = config;
	}
}
