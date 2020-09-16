package com.unn.engine.dataset;

import com.unn.common.dataset.*;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.prediction.Prediction;

import java.util.*;

public class Datasets {

    private static Integer getRewardValue(OuterDataset dataset, ValueMapper mapper, String featureName, int sampleIndex, JobConfig config) {
        Integer featureIndex = dataset.getFeatureIndex(featureName);
        String outerValue = dataset.getFeatureAtSample(sampleIndex, featureIndex);
        Integer innerValue = mapper.getInnerValue(featureName, outerValue);
        Integer refInnerValue = mapper.getInnerValue(config.targetFeature, config.targetOuterValue);
        return JobConfig.mapReward(refInnerValue, innerValue);
    }

    public static InnerDataset toInnerDataset(OuterDataset dataset, ValueMapper mapper, JobConfig job) {
        String timeFeatureName = job.getTimeFeatureName();
        String rewardFeatureName = job.getRewardFeatureName();
        InnerDataset innerDataset = new InnerDataset();

        ArrayList<IOperator> allLeaves = InnerDatasetLoader.getIdentities(
            job, mapper.getFeatures(),
            job.targetFeature, true);
        innerDataset.setAllLeaves(allLeaves);

        ArrayList<IOperator> trainLeaves = InnerDatasetLoader.getIdentities(
                job, mapper.getFeatures(),
                job.targetFeature, false);
        innerDataset.setTrainingLeaves(trainLeaves);

        for (int sampleIndex = 0; sampleIndex < dataset.sampleCount(); ++sampleIndex) {
            Integer timeFeatureIndex = dataset.getFeatureIndex(timeFeatureName);
            Integer outerTimeValue = Integer.parseInt(dataset.getFeatureAtSample(sampleIndex, timeFeatureIndex));
            Integer reward = getRewardValue(dataset, mapper, rewardFeatureName, sampleIndex, job);
            for (int i = 0; i < dataset.featureCount(); ++i) {
                String featureName = dataset.getHeader().get(i);
                String outerValue = dataset.getFeatureAtSample(sampleIndex, i);
                Integer innerValue = mapper.getInnerValue(featureName, outerValue);
                IOperator identity = innerDataset.getFunctorByClassName(featureName);
                ValueTimeReward vtr = new ValueTimeReward(
                        identity, innerValue, outerTimeValue, reward);
                innerDataset.add(vtr);
            }

        }
        return innerDataset;
    }

    public static OuterDataset toOuterDataset(Dataset dataset) {
        OuterDataset outerDataset = new OuterDataset();
        outerDataset.setHeader(dataset.getDescriptor().getHeader().getNames());
        ArrayList<ArrayList<String>> body = new ArrayList<>();
        for (Row row : dataset.getBody().getRows()) {
            ArrayList<String> sample = new ArrayList<>();
            sample.addAll(Arrays.asList(row.getValues()));
            body.add(sample);
        }
        outerDataset.setBody(body);
        return outerDataset;
    }

    public static Dataset toDataset(DatasetDescriptor descriptor, HashMap<String, ArrayList<Prediction>> predictions) {
        Body body = getBodyFromPredictions(descriptor.getHeader().getNames(), predictions);
        Dataset dataset = new Dataset()
            .withDescriptor(descriptor)
            .withBody(body);
        return dataset;
    }

    private static Body getBodyFromPredictions(String[] refs, HashMap<String, ArrayList<Prediction>> predictions) {
        ArrayList<Row> rows = new ArrayList<>();
        for (int i = 0; true; i++) {
            Row row = new Row();
            ArrayList<String> rowVals = new ArrayList<>();
            int j = 0;
            for (String ref : refs) {
                ArrayList<Prediction> refPrediction = predictions.get(ref);
                if (j >= refPrediction.size()) {
                    break;
                }
                Prediction prediction = refPrediction.get(i);
                if (j == 0) {
                    rowVals.add(prediction.getTime().toString());
                }
                rowVals.add(prediction.getValue().toString());
                j++;
            }
            if (rowVals.size() == refs.length+1)  {
                row.withValues(rowVals.toArray(new String[rowVals.size()]));
                rows.add(row);
            } else if (rowVals.size() == 1) {
                break;
            }
        }
        Body body = new Body()
            .withRows(rows.toArray(new Row[rows.size()]));
        return body;
    }
}
