package com.unn.engine.prediction;

import java.util.HashMap;

import com.unn.engine.session.actions.ActionResult;

public class PredictionReport extends ActionResult {
	public HashMap<String, Double> predictions;
	public HashMap<String, int[][]> confusionMatrixes;
	
	public PredictionReport() {
		this.predictions = new HashMap<String, Double>();
		this.confusionMatrixes = new HashMap<String, int[][]>();
	}
	
}
