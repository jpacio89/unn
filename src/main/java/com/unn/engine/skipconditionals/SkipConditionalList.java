package com.unn.engine.skipconditionals;

import com.unn.engine.session.Session;

import java.util.ArrayList;

public class SkipConditionalList {
    ArrayList<SkipConditional> list;
    Session session;
    int bestAccuracy;

    public SkipConditionalList() {
        this.list = new ArrayList<>();
    }

    public ArrayList<SkipConditional> getList() {
        return list;
    }

    public void setList(ArrayList<SkipConditional> list) {
        this.list = list;
    }

    public void add(SkipConditional sc) {
        this.list.add(sc);
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getBestAccuracy() {
        return bestAccuracy;
    }

    public void setBestAccuracy(int bestAccuracy) {
        this.bestAccuracy = bestAccuracy;
    }
}
