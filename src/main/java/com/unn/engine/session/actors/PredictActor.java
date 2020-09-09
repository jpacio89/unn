package com.unn.engine.session.actors;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PredictAction;
import com.unn.engine.prediction.Predictor;

public class PredictActor extends Actor {
	PredictAction action;
	
	public PredictActor(PredictAction action) {
		this.action = action;
	}

	public ActionResult write() {
		Predictor predictor = new Predictor();
    	predictor.init(this.action.getConf(), this.action.getSession());
    	predictor.run();
		return predictor.getReport();
	}
}
