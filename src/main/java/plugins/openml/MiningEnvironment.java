package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;

import unn.dataset.InnerDataset;
import unn.dataset.InnerDatasetLoader;
import unn.dataset.OuterDataset;
import unn.dataset.DatasetParser;
import unn.interfaces.IEnvironment;
import unn.interfaces.IOperator;
import unn.mining.Miner;
import unn.mining.MiningStatusObservable;
import unn.mining.Model;
import unn.mining.StatsWalker;
import unn.mining.Refinery;
import unn.structures.Config;
import unn.structures.Context;
import unn.structures.MiningStatus;

public class MiningEnvironment implements IEnvironment {
	private int datasetId;
	private UnitReport unitReport;
	//private InnerDataset dbDataset;
	// private JobConfig config;
	private Model refinedModel;
	private Context context;
	private JobConfig config;
	private OuterDataset outerDataset;
	private InnerDataset innerDataset;
	
	public MiningEnvironment(OuterDataset outerDataset) {
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
		System.out.println(String.format("[MiningEnvironment] Initializing miner"));
		
		this.config = config;
		this.context = context;
		getStatusObservable().updateStatusLabel("LOADING");
		
		InnerDatasetLoader loader = new InnerDatasetLoader();
		loader.init(this.context, this.config, this.outerDataset);
		this.innerDataset = loader.load();
		this.unitReport = loader.getUnitReport();
	}
	
	public StatsWalker mine() throws Exception {
		this.refinedModel = null;
		
		getStatusObservable().updateStatusLabel("BUFFERING");
		
		Miner miner = new Miner(this.innerDataset, getStatusObservable());
		miner.init();
		
		if (!miner.ready()) {
			System.out.println(String.format(" Not enough data. Skipping..."));
			getStatusObservable().updateStatusLabel("DONE");
			return null;
		}
		
		System.out.println(String.format(" Mining"));
		
		miner.mine();
		
		getStatusObservable().updateStatusLabel("OPTIMIZING");
		
		Model model = miner.getModel();
		Refinery refinery = new Refinery(miner, model);
		
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
	
	public UnitReport getUnitReport() {
		return unitReport;
	}
	
	public Double predict(String key, HashMap<IOperator, Integer> inputs) {
		if (this.refinedModel == null) {
			return null;
		}
		return this.refinedModel.predict(inputs, null, null);
	}
	
	public Model getModel() {
		return this.refinedModel;
	}

	@Override
	public Integer mapInput(String inputString, String version) {
		assert "GradientAsDouble.v1".equals(version);
			
		double value = Double.parseDouble(inputString);
		return DatasetParser.mapPrice(value);
	}
	
	public MiningReport getMiningReport() {
		return null;
	}
	
	public MiningStatus getMiningStatus() {
		return getStatusObservable().getStatus();
	}

	public JobConfig getConfig() {
		return config;
	}

	public void setConfig(JobConfig config) {
		this.config = config;
	}
}
