package com.unn.engine.session.actors;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PublishAction;
import com.unn.engine.session.actions.QueryAction;

public class PublisherActor extends Actor {
	PublishAction action;

	public PublisherActor(PublishAction action) {
		this.action = action;
	}

	public ActionResult write() {
		return null;
	}

	
}
