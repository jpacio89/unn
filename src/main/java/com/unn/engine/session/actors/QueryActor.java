package com.unn.engine.session.actors;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.QueryAction;

public class QueryActor extends Actor {
	QueryAction action;
	
	public QueryActor(QueryAction action) {
		this.action = action;
	}

	public ActionResult write() {
		return null;
	}

	
}
