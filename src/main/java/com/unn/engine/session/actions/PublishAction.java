package com.unn.engine.session.actions;

import com.unn.engine.mining.Model;
import com.unn.engine.session.Session;

public class PublishAction extends Action {
    Session session;

    public PublishAction() { }

    public void setSession(Session session) {
        this.session = session;
    }

    public PublishAction withSession(Session session) {
        setSession(session);
        return this;
    }

    public Session getSession() {
        return session;
    }
}
