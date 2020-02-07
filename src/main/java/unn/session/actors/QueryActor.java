package unn.session.actors;

import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveModelAction;

public class QueryActor extends Actor {
	QueryAction action;
	
	public QueryActor(QueryAction action) {
		this.action = action;
	}

	public ActionResult write() {
		return null;
	}

	
}
