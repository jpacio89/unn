package com.unn.engine;

import com.unn.common.mining.MiningReport;
import com.unn.common.operations.Agent;
import com.unn.common.operations.AgentRole;
import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemDatasetSource;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.Miner;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.mining.PredicateFactory;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TestMiner {
    private Session mine(OuterDataset outerDataset, String target) {
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
        return session;
    }

    private HashMap<Integer, HashMap<String, Double>> predict(Session session, OuterDataset dataset) {
        ValueMapper mapper = session.getInnerDatasetLoader().getValueMapper();
        InnerDataset realtimeInnerDataset = com.unn.engine.dataset.Datasets.toInnerDataset(dataset, mapper);
        session.setOuterDataset(dataset);
        session.getInnerDatasetLoader().getInitialInnerDataset().inject(realtimeInnerDataset);
        ArrayList<Integer> times = session.getInnerDatasetLoader().getInitialInnerDataset().getTimes();

        HashMap<Integer, HashMap<String, Double>> predictions = new HashMap();
        ArrayList<String> featureNames = session.getScopes().keySet().stream().collect(Collectors.toCollection(ArrayList::new));
        Iterator var5 = times.iterator();

        while (var5.hasNext()) {
            Integer time = (Integer)var5.next();
            HashMap<String, Double> row = new HashMap();
            Iterator var8 = featureNames.iterator();

            while(var8.hasNext()) {
                String featureName = (String)var8.next();
                MiningScope scope = session.getScopes().get(featureName);
                Double prediction = scope.getModel().predict(time);
                row.put(featureName, prediction);
            }

            predictions.put(time, row);
        }

        return predictions;
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

    @Test
    public void testStock2() {
        DatasetLocator locator = new FilesystemLocator("/Volumes/Legatron/data/datasets/stock-test-2.csv");
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }

    @Test
    public void testStock3() {
        DatasetLocator locator = new FilesystemLocator("/Volumes/Legatron/data/datasets/stock+test+target=bitcoin+inputs=gold-spx500+memory=21d+future=14d.csv");
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }

    @Test
    public void testStock4() {
        DatasetLocator locator = new FilesystemLocator("/Volumes/Gondor/data/serializations/stock-market-crawler/targets-archive/target-1002/input-1002/dataset.csv");
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }

    @Test
    public void testStock5() {
        DatasetLocator locator = new FilesystemLocator("/Volumes/Gondor/data/serializations/stock-market-crawler/indicators/test-merged.csv");
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }

    @Test
    public void testStock6() {
        DatasetLocator locator = new FilesystemLocator("/Volumes/Gondor/data/serializations/stock-market-crawler/batch-mining/target-1802/input-1802/dataset.csv");
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }

    @Test
    public void testSpaceship() {
        DatasetLocator locatorTrain = new FilesystemLocator("/Volumes/Gondor/data/datasets/spaceship-train.csv");
        FilesystemDatasetProvider providerTrain = new FilesystemDatasetProvider(locatorTrain);
        OuterDataset outerDatasetTrain = providerTrain.load();
        ArrayList<OuterDataset> outerDatasetsTrain = splitDataset(outerDatasetTrain, 100);

        DatasetLocator locatorTest = new FilesystemLocator("/Volumes/Gondor/data/datasets/spaceship-test.csv");
        FilesystemDatasetProvider providerTest = new FilesystemDatasetProvider(locatorTest);
        OuterDataset outerDatasetTest = providerTest.load();

        ArrayList<HashMap<Integer, HashMap<String, Double>>> predictionCluster = new ArrayList<>();
        for (OuterDataset dataset : outerDatasetsTrain) {
            Session session = mine(dataset, "Transported");
            predictionCluster.add(predict (session, outerDatasetTest));
        }

        for (Integer key : predictionCluster.get(0).keySet()) {
            String passengerId = outerDatasetTest.getSampleAsMap(key).get("PassengerId");
            int trueCount = 0;
            int totalCount = 0;
            for (HashMap<Integer, HashMap<String, Double>> predictions : predictionCluster) {
                for (Map.Entry<String, Double> prediction : predictions.get(key).entrySet()) {
                    if (prediction.getKey().contains("discrete_True")) {
                        if (prediction.getValue() != null && prediction.getValue() == 10) {
                            trueCount++;
                        }
                        totalCount++;
                    }
                }
            }
            String prediction = trueCount * 100 / totalCount > 15 ? "True" : "False";
            System.out.printf("%s,%s%n", passengerId, prediction);
        }

        // discrete_labelized_int_0_95a2fd7d72 -> {Double@1392} 10.0
    }

    @Test
    public void testTitanic() {
        DatasetLocator locatorTrain = new FilesystemLocator("/Volumes/Gondor/data/datasets/titanic-train.csv");
        FilesystemDatasetProvider providerTrain = new FilesystemDatasetProvider(locatorTrain);
        OuterDataset outerDatasetTrain = providerTrain.load();
        ArrayList<OuterDataset> outerDatasetsTrain = splitDataset(outerDatasetTrain, 300);

        DatasetLocator locatorTest = new FilesystemLocator("/Volumes/Gondor/data/datasets/titanic-test.csv");
        FilesystemDatasetProvider providerTest = new FilesystemDatasetProvider(locatorTest);
        OuterDataset outerDatasetTest = providerTest.load();

        ArrayList<HashMap<Integer, HashMap<String, Double>>> predictionCluster = new ArrayList<>();
        for (OuterDataset dataset : outerDatasetsTrain) {
            Session session = mine(dataset, "Survived");
            predictionCluster.add(predict (session, outerDatasetTest));
        }

        for (Integer key : predictionCluster.get(0).keySet()) {
            String passengerId = outerDatasetTest.getSampleAsMap(key).get("PassengerId");
            int trueCount = 0;
            int totalCount = 0;
            for (HashMap<Integer, HashMap<String, Double>> predictions : predictionCluster) {
                for (Map.Entry<String, Double> prediction : predictions.get(key).entrySet()) {
                    if (prediction.getKey().contains("discrete_labelized_int_1")) {
                        if (prediction.getValue() != null && prediction.getValue() == 10) {
                            trueCount++;
                        }
                        totalCount++;
                    }
                }
            }
            String prediction = trueCount * 100 / totalCount > 0 ? "1" : "0";
            System.out.printf("%s,%s%n", passengerId, prediction);
        }

        // discrete_labelized_int_0_95a2fd7d72 -> {Double@1392} 10.0
    }

    public ArrayList<OuterDataset> splitDataset(OuterDataset outerDataset, int chunkSize) {
        int chunkCount = 1 + (outerDataset.sampleCount() / chunkSize);
        ArrayList<OuterDataset> datasets = new ArrayList<>();

        for (int i = 0; i < chunkCount; ++i) {
            OuterDataset dataset = new OuterDataset();
            dataset.setBody(new ArrayList<>());
            dataset.setHeader(outerDataset.getHeader());
            datasets.add(dataset);
        }

        for (int i = 0; i < outerDataset.sampleCount(); ++i) {
            datasets.get(i % chunkCount).getBody()
                .add(outerDataset.getBody().get(i));
        }

        return datasets;
    }

    // TODO: test MineAction.splitDataset()
}
