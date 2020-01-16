package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.SaveAction;

public class SaveActor extends Actor {
	SaveAction action;
	
	public SaveActor(SaveAction action) {
		this.action = action;
	}

	public ActionResult run() {
		return null;
	}

	
}
