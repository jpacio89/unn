package plugins.crypto.trade.bot;

import java.util.ArrayList;
import java.util.HashMap;

import plugins.openml.JobConfig;
import plugins.openml.UnitReport;
import unn.Config;
import unn.Dataset;
import unn.DatasetParser;
import unn.IEnvironment;
import unn.IOperator;
import unn.Miner;
import unn.Model;
import unn.ModelRefinery;
import unn.StatsWalker;

public class TradeEnvironment implements IEnvironment {
	HashMap<String, Model> models;
	
	public TradeEnvironment() {
		this.models = new HashMap<String, Model>();
	}
	
	@Override
	public ArrayList<IOperator> getInputs(String market) {
		if (!this.models.containsKey(market)) {
			return null;
		}
		return this.models.get(market).getInputs();
	}
	
	
	public void init() throws Exception {
		ArrayList<String> markets = PostgresManager.getMarkets();
		
		for (String market : markets) {
			System.out.println(String.format("Launching market %s", market));
			System.out.println(String.format(" Select from database"));
			
			Dataset dbDataset = PostgresManager.select(market);
			
			System.out.println(String.format(" Initializing miner"));
			
			Miner miner = new Miner(dbDataset);
			miner.init();
			
			if (!miner.ready()) {
				System.out.println(String.format(" Not enough data. Skipping..."));
				continue;
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
			
			this.models.put(market, refined);
			
			refined.getStatsWalker().printTimes();

			PostgresManager.saveModelStats(market, refined.getStatsWalker());
		}
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

	@Override
	public UnitReport getUnitReport() {
		return null;
	}

	@Override
	public void init(JobConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StatsWalker mine() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatsWalker getStatsWalker() {
		// TODO Auto-generated method stub
		return null;
	}


}
