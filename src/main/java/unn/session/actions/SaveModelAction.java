package unn.session.actions;

import unn.session.Session;

public class SaveModelAction extends Action {
	Session session;
	String pathTemplate;
	
	public SaveModelAction() {
		
	}

	public String getPathTemplate() {
		return pathTemplate;
	}

	public void setPathTemplate(String pathTemplate) {
		this.pathTemplate = pathTemplate;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
}