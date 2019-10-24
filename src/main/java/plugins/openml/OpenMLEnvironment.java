package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;

import unn.Config;
import unn.Dataset;
import unn.DatasetParser;
import unn.IEnvironment;
import unn.IOperator;
import unn.Miner;
import unn.Model;
import unn.ModelRefinery;

public class OpenMLEnvironment implements IEnvironment {
	HashMap<String, Model> models;
	int datasetId;
	UnitReport unitReport;
	
	public OpenMLEnvironment(int datasetId) {
		this.models = new HashMap<String, Model>();
		this.datasetId = datasetId;
	}
	
	@Override
	public ArrayList<IOperator> getInputs(String market) {
		if (!this.models.containsKey(market)) {
			return null;
		}
		return this.models.get(market).getInputs();
	}
	
	
	public void init() throws Exception {
		OpenML ml = new OpenML();
		ml.init();
		
		Dataset dbDataset = ml.getDataset(this.datasetId);
		this.unitReport = ml.getUnitReport();
		
		System.out.println(String.format(" Initializing miner"));
		
		Miner miner = new Miner(dbDataset);
		miner.init();
		
		if (!miner.ready()) {
			System.out.println(String.format(" Not enough data. Skipping..."));
			return;
		}
		
		System.out.println(String.format(" Mining"));
		
		miner.mine();
		
		Model model = miner.getModel();
		
		ModelRefinery refinery = new ModelRefinery(miner, model);
		Model refined = refinery.refine();
		
		int countMin = dbDataset.count(Config.STIMULI_MIN_VALUE);
		int countNull = dbDataset.count(Config.STIMULI_NULL_VALUE);
		int countMax = dbDataset.count(Config.STIMULI_MAX_VALUE);
		
		System.out.println("Min Count = " + countMin);
		System.out.println("Null Count = " + countNull);
		System.out.println("Max Count = " + countMax);
		
		dbDataset.shrink();
		
		refined.getStatsWalker().printTimes();
	}
	
	public UnitReport getUnitReport() {
		return unitReport;
	}
	
	public Double predict(String key, HashMap<IOperator, Integer> inputs) {
		if (!this.models.containsKey(key)) {
			return null;
		}
		return this.models.get(key).predict(inputs);
	}

	@Override
	public Integer mapInput(String inputString, String version) {
		assert "GradientAsDouble.v1".equals(version);
			
		double value = Double.parseDouble(inputString);
		return DatasetParser.mapPrice(value);
	}


}
