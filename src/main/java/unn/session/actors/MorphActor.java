package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.MineAction;
import unn.session.actions.MorphAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveAction;

public class MorphActor extends Actor {
	MorphAction action;
	
	public MorphActor(MorphAction action) {
		this.action = action;
	}

	public ActionResult run() {
		return null;
	}

	
}
