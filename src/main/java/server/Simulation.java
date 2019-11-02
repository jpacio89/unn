package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement.GlobalScope;

import plugins.openml.EnvironmentGroup;
import plugins.openml.MiningEnvironment;
import plugins.openml.SimulationConfig;
import unn.Config;
import unn.IEnvironment;
import unn.IOperator;
import utils.RandomManager;

public class Simulation {
	SimulationConfig config;
	EnvironmentGroup goup;
	SimulationReport report;
	
	public void init(SimulationConfig conf, EnvironmentGroup group) {
		this.config = conf;
		this.goup = group;
	}

	public void run() {
		HashMap<String, MiningEnvironment> envs = goup.getEnvironments();
		this.report = new SimulationReport();
		
		for (Entry<String, MiningEnvironment> env : envs.entrySet()) {
			ArrayList<IOperator> inputs = env.getValue().getInputs("");
	    	
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
	    		} else {
	    			Set<String> possibleValuesSet = possibleValues.keySet();

		    		// TODO: for now we only pick the first one - fix in the future
		    		String seedValueName = possibleValuesSet.iterator().next();
		    		Boolean isOn = possibleValues.get(seedValueName);
		    		
		    		if ("false".equals(seedValueName)) {
		    			isOn = !isOn;
		    		}
		    		
		    		values.put(input, isOn ? Config.STIMULI_MAX_VALUE : Config.STIMULI_MIN_VALUE);
	    		}
	    	}
	    	
	    	//if (values.containsKey(goup.getConfig().targetFeature)) {
	    	//	System.err.println("Result was mistakenly fed into the query.");
	    	//}
	    	
	    	Double prediction = env.getValue().predict("", values);
	    	report.predictions.put(env.getKey(), prediction);
		}
	}

	public SimulationReport getReport() {
		return this.report;
	}

}
