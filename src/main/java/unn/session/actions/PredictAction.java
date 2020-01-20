package unn.session.actions;

import plugins.openml.SimulationConfig;
import unn.session.Session;

public class PredictAction extends Action {
	SimulationConfig conf;
	Session session;
	
	public PredictAction(Session _session, SimulationConfig _conf) {
		this.session = _session;
		this.conf = _conf;
	}

	public SimulationConfig getConf() {
		return conf;
	}

	public void setConf(SimulationConfig conf) {
		this.conf = conf;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
