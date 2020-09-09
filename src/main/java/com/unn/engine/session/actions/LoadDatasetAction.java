package com.unn.engine.session.actions;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.session.Session;
import com.unn.engine.session.Context;

public class LoadDatasetAction extends Action {
	DatasetLocator locator;
	Context context;
	Session session;
	
	public LoadDatasetAction(DatasetLocator _locator) {
		this.locator = _locator;
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
