package unn.session.actions;

import unn.dataset.DatasetLocator;
import unn.session.Session;
import unn.structures.Context;

public class LoadAction extends Action {
	DatasetLocator locator;
	Context context;
	Session session;
	
	public LoadAction(Context _context, Session _session, DatasetLocator _locator) {
		this.locator = _locator;
		this.context = _context;
		this.session = _session;
	}

	public DatasetLocator getLocator() {
		return locator;
	}

	public void setLocator(DatasetLocator locator) {
		this.locator = locator;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	
}
