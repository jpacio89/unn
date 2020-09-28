package com.unn.engine.session.actors;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.LoadDatasetAction;

public class LoadActor extends Actor {
	LoadDatasetAction action;
	
	public LoadActor(LoadDatasetAction action) {
		this.action = action;
	}

	public ActionResult write() {
    	this.action.getSession().loadOuterDataset(this.action.getLocator());
		return null;
	}

	
}
