package com.unn.engine.session.actions;

import com.unn.engine.prediction.PredictionConfig;
import com.unn.engine.session.Session;

public class PredictAction extends Action {
	PredictionConfig conf;
	Session session;
	
	public PredictAction(Session _session, PredictionConfig _conf) {
		this.session = _session;
		this.conf = _conf;
	}

	public PredictionConfig getConf() {
		return conf;
	}

	public void setConf(PredictionConfig conf) {
		this.conf = conf;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
