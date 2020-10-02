package com.unn.engine.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.session.Session;
import com.unn.engine.Config;
import com.unn.engine.utils.RandomManager;

public class Predictor {
	PredictionConfig config;
	Session session;
	PredictionReport report;
	
	public void init(PredictionConfig conf, Session session) {
		this.config = conf;
		this.session = session;
	}

	public void run() {
		HashMap<String, MiningScope> envs = session.getScopes();
		this.report = new PredictionReport();
		
		for (Entry<String, MiningScope> env : envs.entrySet()) {
			ArrayList<IOperator> inputs = env.getValue().getInputs("");
			ValueMapper unitReport = env.getValue().getMapper();
			
	    	if (inputs == null) {
	    		continue;
	    	}
	    	
	    	HashMap<IOperator, Integer> values = new HashMap<IOperator, Integer>();
	    	
	    	for (IOperator input : inputs) {
	    		HashMap<String, Boolean> possibleValues = this.config.seeds.get(input.toString());
	    		
	    		if (possibleValues == null) {
	    			ArrayList<Integer> rnds = new ArrayList<Integer>();
	    			// TODO: fix this for more than boolean features
	    			rnds.add(Config.STIM_MIN);
	    			rnds.add(Config.STIM_MAX);
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
	    	
	    	Double prediction = env.getValue().predict(values);
	    	this.report.predictions.put(env.getKey(), prediction);
	    	this.report.confusionMatrixes.put(env.getKey(), env.getValue().getStatsWalker().getHitMatrix());
		}
	}

	public PredictionReport getReport() {
		return this.report;
	}

}
