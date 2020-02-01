package unn.session.actions;

import unn.morphing.MorphConfig;
import unn.session.Session;

public class MorphAction extends Action {
	MorphConfig conf;
	Session session;
	
	public MorphAction(Session _session, MorphConfig _conf) {
		this.session = _session;
		this.conf = _conf;
	}

	public MorphConfig getConf() {
		return conf;
	}

	public void setConf(MorphConfig conf) {
		this.conf = conf;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
