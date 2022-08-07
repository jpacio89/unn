package com.unn.engine.session;

import java.io.Serializable;
import java.util.HashMap;

import com.unn.common.operations.AgentRole;

public class Context implements Serializable {
	private HashMap<String, Session> sessions;
	private Session liveSession;

	public Context() {
		this.sessions = new HashMap<>();
	}

	public void setRole(AgentRole role) {
		if (this.liveSession != null && this.liveSession.isAlive()) {
			return;
		}
		this.liveSession = new Session(this, role);
		this.liveSession.processRole();
		this.sessions.put(role.getId(), this.liveSession);
	}

	public Session getLiveSession() {
		if (this.liveSession != null && !this.liveSession.isAlive()) {
			this.liveSession = null;
		}
		return this.liveSession;
	}
}
