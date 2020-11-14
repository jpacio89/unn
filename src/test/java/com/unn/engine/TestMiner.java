package com.unn.engine;

import com.unn.common.mining.MiningReport;
import com.unn.common.operations.Agent;
import com.unn.common.operations.AgentRole;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.mining.MinerXGBoost;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;
import org.junit.Test;

import java.util.ArrayList;

public class TestMiner {

    @Test
    public void testXGBoost() {
        MinerXGBoost miner = new MinerXGBoost();
        miner.mine();
    }

    @Test
    public void testCircleWheats() {
        OuterDataset outerDataset = new OuterDataset();
        String[] features = { "id", "primer", "x", "y"/*, "distance"*/, "reward" };
        outerDataset.setHeader(features);

        for (int i = 0; i < 100; ++i) {
            double x = Math.random();
            double y = Math.random();
            double distance = Math.sqrt(x * x + y * y);
            String reward = distance > 0.5 ? "T" : "F";
            String[] row = { Integer.toString(i), Integer.toString(i),
                    Double.toString(x), Double.toString(y)/*, Double.toString(distance)*/, reward };
            outerDataset.addSample(row);
        }

        Context context = new Context();
        AgentRole role = new AgentRole();
        Session session = new Session(context, role);
        session.setOuterDataset(outerDataset);

        MineAction action = new MineAction();
        action.setSession(session);
        action.setConf(new JobConfig("reward", new ArrayList<>()));

        action.act();

        MiningReport report = action.getSession().getReport();
        if (report == null || report.getConfusionMatrixes().size() == 0) {
            System.out.println("Report statistics --> null");
        } else {
            System.out.println(String.format("Report statistics -->\n%s", report.toString()));
        }
    }
}
