package com.unn.engine.session.actors;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.PredictAction;

public class FeedforwardServeActor extends Actor {
	PredictAction action;
	
	public FeedforwardServeActor(PredictAction action) {
		this.action = action;
	}

	public ActionResult write() {
		return null;
	}
	
	private void getDescriptor() {
		// TODO: implement
	}
	
	private void serve(String request) {
		// TODO: implement
	}
	
	private void push(String response) {
		
	}
	
	private void subscribe(String response) {
		
	}
}
