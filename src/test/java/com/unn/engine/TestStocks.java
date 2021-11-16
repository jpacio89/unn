package com.unn.engine;

import com.unn.common.dataset.*;
import com.unn.common.mining.MiningReport;
import com.unn.common.operations.AgentRole;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Serializer;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;
import org.junit.Test;
import org.scalatest.Entry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestStocks {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

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

        return session;
    }

    private void writeReportToFile(File inputFolder, Session session) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(String.format("%s/report.log", inputFolder.getAbsolutePath())));
            writer.write(session.getReport().toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<Integer, HashMap<String, Double>> predictFromSession(ArrayList<Integer> times, Session session) {
        ArrayList<String> validScopes = session.getReport().getConfusionMatrixes().entrySet().stream()
                .filter(entry -> entry.getValue().getTpCount() >= 20 &&
                        entry.getValue().getTpr() - entry.getValue().getPr() >= 20 &&
                        entry.getValue().getTpr() > 70)
                .map(entry -> entry.getKey())
                .collect(Collectors.toCollection(ArrayList::new));

        if (validScopes.size() == 0) {
            //System.out.println("Scope not good enough.");
            //return null;
        }

        HashMap<Integer, HashMap<String, Double>> predictions = new HashMap();
        ArrayList<String> featureNames = session.getScopes().keySet().stream()
            .filter(featureName -> validScopes.contains(featureName))
            .collect(Collectors.toCollection(ArrayList::new));

        for (Integer time : times) {
            HashMap<String, Double> row = new HashMap<>();
            for (String featureName : featureNames) {
                MiningScope scope = session.getScopes().get(featureName);
                Double prediction = scope.getModel().predict(time);
                row.put(featureName, prediction);
            }
            predictions.put(time, row);
        }

        return predictions;
    }

    private void buildLayer2Dataset(Session pivot, ArrayList<HashMap<Integer, HashMap<String, Double>>> predictionsBatch) {
        ArrayList<String> featureNames = new ArrayList<>();
        featureNames.add("id");
        featureNames.add("primer");

        Row[] rows = new Row[pivot.getOuterDataset().sampleCount()];
        ArrayList<ArrayList<String>> batchFeatures = new ArrayList<>();

        for (int i = 0; i < pivot.getOuterDataset().sampleCount(); ++i) {
            HashMap<String, String> sample = pivot.getOuterDataset().getSampleAsMap(i);
            String outcome = sample.get("outcome");
            Integer primer = Integer.parseInt(sample.get("primer"));

            ArrayList<String> rowValues = new ArrayList<>();
            rowValues.add(Integer.toString(i + 1));
            rowValues.add(Integer.toString(primer));

            for (int j = 0; j < predictionsBatch.size(); ++j) {
                HashMap<Integer, HashMap<String, Double>> row = predictionsBatch.get(j);
                HashMap<String, Double> timedPredictions = row.get(primer);

                if (i == 0) {
                    ArrayList<String> colNames = new ArrayList<>();
                    colNames.addAll(timedPredictions.keySet().stream()
                        .collect(Collectors.toCollection(ArrayList::new)));
                    featureNames.addAll(colNames);
                    batchFeatures.add(colNames);
                }

                for (String feature : batchFeatures.get(j)) {
                    Double prediction = timedPredictions.get(feature);
                    if (prediction == null) {
                        rowValues.add("?");
                    } else if (prediction == Config.get().STIM_MIN) {
                        rowValues.add("F");
                    } else if (prediction == Config.get().STIM_MAX) {
                        rowValues.add("T");
                    } else if (prediction == Config.get().STIM_NULL) {
                        rowValues.add("N");
                    } else {
                        rowValues.add(Double.toString(prediction));
                    }
                }
            }

            rowValues.add(outcome);
            rows[i] = new Row()
                .withValues(rowValues.stream()
                    .toArray(String[]::new));
        }

        featureNames.add("outcome");

        Body body = new Body()
            .withRows(rows);
        Dataset dataset = new Dataset()
            .withBody(body)
            .withDescriptor(new DatasetDescriptor()
                .withHeader(new Header()
                    .withNames(featureNames.stream()
                        .toArray(String[]::new))));

        new CSVHelper().writeToFile(String.format("%s/target-1001/layer2.csv", folderPath), dataset);
    }

    @Test
    public void testBatchMining_target1001_layer2() {
        ArrayList<Integer> times = null;
        Session pivotSession = (Session) Serializer.read(String.format("%s/target-1001/input-1001/predictor", folderPath), "session");
        for (Map.Entry<String, MiningScope> entry : pivotSession.getScopes().entrySet()) {
            times = entry.getValue().getModel().getDataset().getTimes();
            break;
        }

        String dataSourcePath = String.format("%s/target-1001", folderPath);
        File folder = new File(dataSourcePath);

        ArrayList<HashMap<Integer, HashMap<String, Double>>> predictionBatch = new ArrayList<>();
        ArrayList<File> croppedFiles = Arrays.stream(folder.listFiles())
//            .limit(3)
            .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < croppedFiles.size(); ++i) {
            File inputFolder = croppedFiles.get(i);
            if (!inputFolder.isDirectory() ||
                !inputFolder.getName().startsWith("input-")) {
                continue;
            }
            File sessionFile = new File(String.format("%s/predictor.v1.session", inputFolder.getAbsolutePath()));
            if (sessionFile.exists()) {
                System.out.println(
                    String.format("|BatchMiner| Reading %s (%d of %d)",
                        inputFolder.getName(), i + 1, croppedFiles.size()));
            }

            Session session = (Session) Serializer.read(String.format("%s/predictor", inputFolder.getAbsolutePath()), "session");
            HashMap<Integer, HashMap<String, Double>> predictions = predictFromSession(times, session);

            if (predictions != null) {
                predictionBatch.add(predictions);
                System.out.println(predictions);
            }
        }

        buildLayer2Dataset(pivotSession, predictionBatch);
    }

    @Test
    public void testBatchMining_target1001() {
        String dataSourcePath = String.format("%s/target-1001", folderPath);
        File folder = new File(dataSourcePath);

        for (File inputFolder : folder.listFiles()) {
            if (!inputFolder.isDirectory() ||
                !inputFolder.getName().startsWith("input-")) {
                continue;
            }
            File sessionFile = new File(String.format("%s/predictor.v1.session", inputFolder.getAbsolutePath()));
            if (sessionFile.exists()) {
                System.out.println(String.format("|BatchMiner| Skipping %s", inputFolder.getName()));
                continue;
            }
            System.out.printf("|BatchMiner| Mining %s%n", inputFolder.getName());
            String datasetPath = String.format("%s/dataset.csv", inputFolder.getAbsolutePath());
            DatasetLocator locator = new FilesystemLocator(datasetPath);
            FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
            OuterDataset outerDataset = provider.load();
            Session session = mine(outerDataset, "outcome");
            Serializer.write(session, String.format("%s/predictor", inputFolder.getAbsolutePath()), "session");
            this.writeReportToFile(inputFolder, session);
        }
    }

    @Test
    public void testStockLayer2() {
        String dataSourcePath = String.format("%s/target-1001/layer2.csv", folderPath);
        DatasetLocator locator = new FilesystemLocator(dataSourcePath);
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        mine(outerDataset, "outcome");
    }
}
