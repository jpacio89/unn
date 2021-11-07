package com.unn.engine;

import com.unn.common.mining.MiningReport;
import com.unn.common.operations.Agent;
import com.unn.common.operations.AgentRole;
import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemDatasetSource;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.mining.Miner;
import com.unn.engine.mining.PredicateFactory;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class TestMiner {
    private void mine(OuterDataset outerDataset, String target) {
        Context context = new Context();
        AgentRole role = new AgentRole();
        Session session = new Session(context, role);
        session.setOuterDataset(outerDataset);

        MineAction action = new MineAction();
        action.setSession(session);
        action.setConf(new JobConfig(target, new ArrayList<>()));

        action.act();

        MiningReport report = action.getSession().getReport();
        if (report == null || report.getConfusionMatrixes().size() == 0) {
            System.out.println("Report statistics --> null");
        } else {
            System.out.println(String.format("Report statistics -->\n%s", report.toString()));
        }

        checkNoMiningGroupsNotUsed(session);
    }

    private void checkNoMiningGroupsNotUsed(Session session) {
        // Note: make sure the predicate factories do not use the target feature (and blacklisted features)
        session.getScopes().values().forEach(scope -> {
            Miner miner = scope.getMiner();
            ArrayList<IFeature> noMiningFeatures = scope.getConfig().getNoMiningGroups();
            ArrayList<PredicateFactory> factories = miner.getPredicateFactories();

            assertTrue(noMiningFeatures.contains(miner.getMiningTarget()));

            factories.forEach(factory -> {
                assertTrue(noMiningFeatures.stream()
                    .filter(group -> factory.getFeatures().contains(group))
                    .count() == 0);
            });
        });
    }

    @Test
    public void testCircleWheats() {
        OuterDataset outerDataset = new OuterDataset();
        String[] features = { "id", "primer", "x", "y", "distance", "reward" };
        outerDataset.setHeader(features);

        for (int i = 0; i < 1000; ++i) {
            double x = Math.random();
            double y = Math.random();
            double distance = Math.sqrt(x * x + y * y);
            String reward = distance > 0.5 ? "T" : "F";
            double uncertainty = Math.random();
            if (uncertainty > .75) {
                reward = reward.equals("T") ? "F" : "T";
            }
            String[] row = { Integer.toString(i), Integer.toString(i),
                Double.toString(x), Double.toString(y), Double.toString(distance), reward };
            outerDataset.addSample(row);
        }

        mine(outerDataset, "reward");
    }

    @Test
    public void testZoo() {
        OuterDataset outerDataset = Datasets.dummy3();
        mine(outerDataset, "type");
    }

    @Test
    public void testTicTacToe() {
        OuterDataset outerDataset = Datasets.dummy2();
        mine(outerDataset, "Class");
    }

    @Test
    public void testSinusoideWheats() {
        OuterDataset outerDataset = new OuterDataset();
        String[] features = { "id", "primer", "x", "y", /*"mod", */"reward" };
        outerDataset.setHeader(features);

        for (int i = 0; i < 1000; ++i) {
            double x = Math.random() * 360;
            double y = 2 * Math.random() - 1;
            String reward = y > Math.sin(2 * (Math.PI * x / 360.0)) ? "T" : "F";
            //double mod = Math.sin(2 * (Math.PI * x / 360.0));
            //double uncertainty = Math.random();
            //if (uncertainty > .75) {
            //    reward = reward.equals("T") ? "F" : "T";
            //}
            String[] row = { Integer.toString(i), Integer.toString(i),
                    Double.toString(x), Double.toString(y), /*Double.toString(mod),*/ reward };
            outerDataset.addSample(row);
        }

        mine(outerDataset, "reward");
    }

    @Test
    public void testStock1() {
        DatasetLocator locator = new FilesystemLocator("/Volumes/Legatron/data/datasets/stock-test-1.csv");
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }

    // TODO: test MineAction.splitDataset()
}
