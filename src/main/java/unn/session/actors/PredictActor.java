package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.MineAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveAction;
import unn.simulation.Prediction;
import unn.simulation.SimulationReport;

public class PredictActor extends Actor {
	PredictAction action;
	
	public PredictActor(PredictAction action) {
		this.action = action;
	}

	public ActionResult run() {
		Prediction prediction = new Prediction();
    	prediction.init(this.action.getConf(), this.action.getSession());
    	prediction.run();
		return prediction.getReport();
	}
}
