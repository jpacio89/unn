package com.unn.engine.session.actions;

import com.unn.engine.session.Session;
import com.unn.engine.session.Context;

public class LoadModelAction {
	String pathTemplate;
	
	public LoadModelAction(Context _context, Session _session, String pathTemplate) {
		this.pathTemplate = pathTemplate;
	}

	public String getPathTemplate() {
		return pathTemplate;
	}

	public void setPathTemplate(String pathTemplate) {
		this.pathTemplate = pathTemplate;
	}
	
	
}
