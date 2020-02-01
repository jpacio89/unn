package unn.session.actors;

import unn.morphing.Morpher;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.MineAction;
import unn.session.actions.MorphAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveAction;
import unn.simulation.Prediction;

public class MorphActor extends Actor {
	MorphAction action;
	
	public MorphActor(MorphAction action) {
		this.action = action;
	}

	public ActionResult run() {
		Morpher morpher = new Morpher();
    	morpher.init(this.action.getConf(), this.action.getSession());
    	morpher.morph();
		//return morpher.getReport();
    	return null;
	}

	
}
