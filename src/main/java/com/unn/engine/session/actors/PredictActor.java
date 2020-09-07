package com.unn.engine.session.actors;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PredictAction;
import com.unn.engine.prediction.Prediction;

public class PredictActor extends Actor {
	PredictAction action;
	
	public PredictActor(PredictAction action) {
		this.action = action;
	}

	public ActionResult write() {
		Prediction prediction = new Prediction();
    	prediction.init(this.action.getConf(), this.action.getSession());
    	prediction.run();
		return prediction.getReport();
	}
}
