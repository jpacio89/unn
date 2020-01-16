package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveAction;

public class QueryActor extends Actor {
	QueryAction action;
	
	public QueryActor(QueryAction action) {
		this.action = action;
	}

	public ActionResult run() {
		return null;
	}

	
}
