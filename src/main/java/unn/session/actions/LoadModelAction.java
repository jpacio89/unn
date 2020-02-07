package unn.session.actions;

import unn.session.Session;
import unn.structures.Context;

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
