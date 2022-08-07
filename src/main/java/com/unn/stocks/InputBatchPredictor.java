package com.unn.stocks;

import com.unn.common.dataset.*;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Serializer;
import com.unn.engine.Config;
import com.unn.engine.dataset.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.OuterDatasetLoader;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.session.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class InputBatchPredictor {
    private String folderPath;
    private String targetInstrumentId;
    private boolean isRealTime;

    public InputBatchPredictor(String _folderPath, String _targetInstrumentId, boolean _isRealTime) {
        this.folderPath = _folderPath;
        this.targetInstrumentId = _targetInstrumentId;
        this.isRealTime = _isRealTime;
    }

    public InputBatchPredictor start() {
        Session pivotSession = (Session) Serializer.read(
            String.format("%s/input-%s/predictor",
                this.folderPath, this.targetInstrumentId), "session");

        processInnerDataset(pivotSession,
                String.format("%s/input-%s", folderPath, targetInstrumentId));
        ArrayList<Integer> times = pivotSession.getInnerDatasetLoader().getInitialInnerDataset().getTimes();

        File folder = new File(this.folderPath);

        ArrayList<HashMap<Integer, HashMap<String, Double>>> predictionBatch = new ArrayList<>();
        ArrayList<File> croppedFiles = Arrays.stream(folder.listFiles())
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

            Session session = (Session) Serializer.read(String.format("%s/predictor",
                inputFolder.getAbsolutePath()), "session");
            processInnerDataset(session, inputFolder.getAbsolutePath());
            HashMap<Integer, HashMap<String, Double>> predictions = this.predictFromSession(times, session);

            if (predictions != null) {
                predictionBatch.add(predictions);
                System.out.println(predictions);
            }
        }

        this.createOutputDataset(pivotSession, predictionBatch);
        return this;
    }

    private HashMap<Integer, HashMap<String, Double>> predictFromSession(ArrayList<Integer> times, Session session) {
        //ArrayList<String> validScopes = session.getReport().getConfusionMatrixes().entrySet().stream()
        //        .filter(entry -> entry.getValue().getTpCount() >= 20 &&
        //                entry.getValue().getTpr() - entry.getValue().getPr() >= 20 &&
        //                entry.getValue().getTpr() > 70)
        //        .map(entry -> entry.getKey())
        //        .collect(Collectors.toCollection(ArrayList::new));

        //if (validScopes.size() == 0) {
        //  System.out.println("Scope not good enough.");
        //  return null;
        //}

        HashMap<Integer, HashMap<String, Double>> predictions = new HashMap();
        ArrayList<String> featureNames = session.getScopes().keySet().stream()
//            .filter(featureName -> validScopes.contains(featureName))
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

    private void createOutputDataset(Session pivot, ArrayList<HashMap<Integer, HashMap<String, Double>>> predictionsBatch) {
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

        if (this.isRealTime) {
            new CSVHelper().writeToFile(
                    String.format("%s/output/input-1", this.folderPath), "realtime.csv", dataset);
        } else {
            new CSVHelper().writeToFile(
                    String.format("%s/output/input-1", this.folderPath), "dataset.csv", dataset);
        }
    }

    private void processInnerDataset(Session session, String basePath) {
        if (session.getInnerDatasetLoader() == null) {
            return;
        }
        if (this.isRealTime) {
            ValueMapper mapper = session.getInnerDatasetLoader().getValueMapper();
            OuterDataset realtimeDataset = this.loadRealtimeDataset(basePath);
            InnerDataset realtimeInnerDataset = Datasets.toInnerDataset(realtimeDataset, mapper);
            session.setOuterDataset(realtimeDataset);
            session.getInnerDatasetLoader().getInitialInnerDataset().inject(realtimeInnerDataset);
        } else {
            session.getInnerDatasetLoader().reconstruct();
        }
    }

    private OuterDataset loadRealtimeDataset(String datasetPath) {
        try {
            String realtimeDatasetPath = String.format("%s/realtime.csv", datasetPath);
            OuterDataset realtimeOuterDataset = new OuterDatasetLoader()
                    .load(new FilesystemLocator(realtimeDatasetPath));
            return realtimeOuterDataset;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
