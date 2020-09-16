package com.unn.engine.session.actions;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.mining.Model;
import com.unn.engine.session.Session;

public class PublishAction extends Action {
    Session session;
    int upstreamLayer;
    private DatasetLocator datasetLocator;

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

    public int getUpstreamLayer() {
        return upstreamLayer;
    }

    public void setUpstreamLayer(int upstreamLayer) {
        this.upstreamLayer = upstreamLayer;
    }

    public void setDatasetLocator(DatasetLocator datasetLocator) {
        this.datasetLocator = datasetLocator;
    }

    public DatasetLocator getDatasetLocator() {
        return datasetLocator;
    }
}
