package unn.simulation;

import java.util.HashMap;

import unn.session.actions.ActionResult;

public class SimulationReport extends ActionResult {
	public HashMap<String, Double> predictions;
	
	public SimulationReport() {
		this.predictions = new HashMap<String, Double>();
	}
	
}
