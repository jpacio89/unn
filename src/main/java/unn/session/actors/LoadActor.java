package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;

public class LoadActor extends Actor {
	LoadAction action;
	
	public LoadActor(LoadAction action) {
		this.action = action;
	}

	public ActionResult run() {
		return null;
	}

	
}
