package com.unn.engine.session;

import java.io.Serializable;
import com.unn.common.operations.AgentRole;

public class Context implements Serializable {
	private Session session;

	public Context() { }

	public void setRole(AgentRole role) {
		this.session = new Session(this, role);
		this.session.processRole();
	}

	public Session getActiveSession() {
		return this.session;
	}
}
