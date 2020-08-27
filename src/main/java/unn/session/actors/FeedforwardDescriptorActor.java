package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.MineAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveModelAction;
import unn.simulation.Prediction;
import unn.simulation.SimulationReport;

public class FeedforwardDescriptorActor extends Actor {
	PredictAction action;
	
	public FeedforwardDescriptorActor(PredictAction action) {
		this.action = action;
	}

	public ActionResult write() {
		return null;
	}
	
	private void getDescriptor() {
		// TODO: implement
	}
	
	private void serve(String request) {
		// TODO: implement
	}
	
	private void push(String response) {
		
	}
	
	private void subscribe(String response) {
		
	}
}
