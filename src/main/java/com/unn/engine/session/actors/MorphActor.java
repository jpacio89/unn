package com.unn.engine.session.actors;

import com.unn.engine.morphing.Morpher;
import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.MorphAction;

public class MorphActor extends Actor {
	MorphAction action;
	
	public MorphActor(MorphAction action) {
		this.action = action;
	}

	public ActionResult write() {
		Morpher morpher = new Morpher();
    	morpher.init(this.action.getConf(), this.action.getSession());
    	morpher.morph();
		//return morpher.getReport();
    	return null;
	}

	
}
