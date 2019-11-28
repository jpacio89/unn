package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;

import unn.dataset.Dataset;
import unn.dataset.DatasetParser;
import unn.interfaces.IEnvironment;
import unn.interfaces.IOperator;
import unn.mining.Miner;
import unn.mining.MiningStatusObservable;
import unn.mining.Model;
import unn.mining.StatsWalker;
import unn.mining.Refinery;
import unn.structures.Config;
import unn.structures.MiningStatus;

public class MiningEnvironment implements IEnvironment {
	private int datasetId;
	private UnitReport unitReport;
	private Dataset dbDataset;
	// private JobConfig config;
	private Model refinedModel;
	private MiningStatusObservable statusObservable;
	
	public MiningEnvironment(int datasetId) {
		this.datasetId = datasetId;
		this.statusObservable = new MiningStatusObservable();
	}
	
	@Override
	public ArrayList<IOperator> getInputs(String market) {
		if (this.refinedModel == null) {
			return null;
		}
		return this.refinedModel.getInputs();
	}
	
	
	public void init(JobConfig config) {
		// this.config = config;
		this.statusObservable.updateStatusLabel("LOADING");
		
		OpenML ml = new OpenML();
		ml.init(config);
		
		System.out.println(String.format(" Initializing miner"));
		
		this.dbDataset = ml.getDataset(this.datasetId);
		this.unitReport = ml.getUnitReport();
	}
	
	public StatsWalker mine() throws Exception {
		this.refinedModel = null;
		
		this.statusObservable.updateStatusLabel("CACHING");
		
		Miner miner = new Miner(dbDataset, statusObservable);
		miner.init();
		
		if (!miner.ready()) {
			System.out.println(String.format(" Not enough data. Skipping..."));
			return null;
		}
		
		System.out.println(String.format(" Mining"));
		
		miner.mine();
		
		this.statusObservable.updateStatusLabel("OPTIMIZING");
		
		Model model = miner.getModel();
		
		//ModelRefinery refinery = new ModelRefinery(miner, model);
		Refinery refinery = new Refinery(miner, model);
		//this.refinedModel = refinery.refine();
		this.refinedModel = model;
		
		int countMin = dbDataset.count(Config.STIMULI_MIN_VALUE);
		int countNull = dbDataset.count(Config.STIMULI_NULL_VALUE);
		int countMax = dbDataset.count(Config.STIMULI_MAX_VALUE);
		
		System.out.println("Min Count = " + countMin);
		System.out.println("Null Count = " + countNull);
		System.out.println("Max Count = " + countMax);
		
		dbDataset.shrink();
		
		this.statusObservable.updateStatusLabel("DONE");
		
		// refined.getStatsWalker().printTimes();
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
		return this.refinedModel.predict(inputs);
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
		return this.statusObservable.getStatus();
	}
}
