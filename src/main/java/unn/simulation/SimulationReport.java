package unn.simulation;

import java.util.HashMap;

import unn.session.actions.ActionResult;

public class SimulationReport extends ActionResult {
	public HashMap<String, Double> predictions;
	public HashMap<String, int[][]> confusionMatrixes;
	
	public SimulationReport() {
		this.predictions = new HashMap<String, Double>();
		this.confusionMatrixes = new HashMap<String, int[][]>();
	}
	
}
