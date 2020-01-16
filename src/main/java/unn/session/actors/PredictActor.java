package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.MineAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveAction;

public class PredictActor extends Actor {
	PredictAction action;
	
	public PredictActor(PredictAction action) {
		this.action = action;
	}

	public ActionResult run() {
		return null;
	}

	
}
