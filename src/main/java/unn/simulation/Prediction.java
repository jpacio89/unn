package unn.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import plugins.openml.EnvironmentGroup;
import plugins.openml.MiningEnvironment;
import plugins.openml.SimulationConfig;
import plugins.openml.UnitReport;
import unn.interfaces.IOperator;
import unn.session.Session;
import unn.structures.Config;
import utils.RandomManager;

public class Prediction {
	SimulationConfig config;
	Session session;
	SimulationReport report;
	
	public void init(SimulationConfig conf, Session session) {
		this.config = conf;
		this.session = session;
	}

	public void run() {
		HashMap<String, MiningEnvironment> envs = session.getEnvs();
		this.report = new SimulationReport();
		
		for (Entry<String, MiningEnvironment> env : envs.entrySet()) {
			ArrayList<IOperator> inputs = env.getValue().getInputs("");
			UnitReport unitReport = env.getValue().getUnitReport();
			
	    	if (inputs == null) {
	    		continue;
	    	}
	    	
	    	HashMap<IOperator, Integer> values = new HashMap<IOperator, Integer>();
	    	
	    	for (IOperator input : inputs) {
	    		HashMap<String, Boolean> possibleValues = this.config.seeds.get(input.toString());
	    		
	    		if (possibleValues == null) {
	    			ArrayList<Integer> rnds = new ArrayList<Integer>();
	    			// TODO: fix this for more than boolean features
	    			rnds.add(Config.STIMULI_MIN_VALUE);
	    			rnds.add(Config.STIMULI_MAX_VALUE);
	    			Integer guess = RandomManager.getOne(rnds);
	    			values.put(input, guess);
	    			
	    			System.err.println("Fix this shit!!");
	    		} else {
	    			Set<String> possibleValuesSet = possibleValues.keySet();

		    		// TODO: fix this shit!!!!!
	    			for (String seedValueName : possibleValuesSet) {
			    		Boolean isOn = possibleValues.get(seedValueName);
			    		
			    		if (!isOn) {
			    			continue;
			    		}
			    		
			    		Integer rewardInnerValue = unitReport.getInnerValue(input.toString(), seedValueName);
			    		values.put(input, rewardInnerValue);
			    		break;
	    			}
	    		}
	    	}
	    	
	    	//if (values.containsKey(goup.getConfig().targetFeature)) {
	    	//	System.err.println("Result was mistakenly fed into the query.");
	    	//}
	    	
	    	Double prediction = env.getValue().predict("", values);
	    	this.report.predictions.put(env.getKey(), prediction);
	    	this.report.confusionMatrixes.put(env.getKey(), env.getValue().getStatsWalker().getHitMatrix());
		}
	}
	
	/*public void morph() {
		HashMap<String, MiningEnvironment> envs = session.getEnvironments();
		this.report = new SimulationReport();
		
		for (Entry<String, MiningEnvironment> env : envs.entrySet()) {
			String feature = env.getKey();
			ArrayList<IOperator> inputs = env.getValue().getInputs("");
			UnitReport unitReport = env.getValue().getUnitReport();
			
	    	if (inputs == null) {
	    		continue;
	    	}
	    	
	    	HashMap<IOperator, Integer> values = new HashMap<IOperator, Integer>();
	    	
	    	for (IOperator input : inputs) {
	    		HashMap<String, Boolean> possibleValues = this.config.seeds.get(input.toString());
	    		
	    		if (possibleValues == null) {
	    			ArrayList<Integer> rnds = new ArrayList<Integer>();
	    			// TODO: fix this for more than boolean features
	    			rnds.add(Config.STIMULI_MIN_VALUE);
	    			rnds.add(Config.STIMULI_MAX_VALUE);
	    			Integer guess = RandomManager.getOne(rnds);
	    			values.put(input, guess);
	    			
	    			System.err.println("Fix this shit!!");
	    		} else {
	    			Set<String> possibleValuesSet = possibleValues.keySet();

		    		// TODO: fix this shit!!!!!
	    			for (String seedValueName : possibleValuesSet) {
			    		Boolean isOn = possibleValues.get(seedValueName);
			    		
			    		if (!isOn) {
			    			continue;
			    		}
			    		
			    		Integer rewardInnerValue = unitReport.getInnerValue(input.toString(), seedValueName);
			    		values.put(input, rewardInnerValue);
			    		break;
	    			}
	    		}
	    	}
	    	
	    	//if (values.containsKey(goup.getConfig().targetFeature)) {
	    	//	System.err.println("Result was mistakenly fed into the query.");
	    	//}
	    	
	    	// TODO: fix this - make generic
	    	HashMap<String, Boolean> classValue = this.config.seeds.get("\"type\"");
	    	for (String type : classValue.keySet()) {
	    		session.morph(values, type, "mammal");
	    		break;
	    	}
	    	
	    	break;
		}
	}*/

	public SimulationReport getReport() {
		return this.report;
	}

}
