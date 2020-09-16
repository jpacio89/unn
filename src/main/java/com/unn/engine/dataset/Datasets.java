package com.unn.engine.dataset;

import com.unn.common.dataset.*;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.prediction.Prediction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

    public static Dataset toDataset(DatasetDescriptor upstreamDescriptor, HashMap<String, ArrayList<Prediction>> predictions) {
        String namespace = "com.example.thing";
        ArrayList<String> refs = new ArrayList<>();
        refs.add("primary");
        for (String ref : predictions.keySet()) {
            refs.add(ref);
        }
        String[] names = refs.toArray(new String[refs.size()]);
        String description = "Mined dataset";
        // upstreamDescriptor.getNamespace()
        DatasetDescriptor descriptor = new DatasetDescriptor()
            .withLayer(upstreamDescriptor.getLayer()+1)
            .withHeader(new Header().withNames(names))
            .withNamespace(namespace)
            .withUpstreamDependencies(upstreamDescriptor.getUpstreamDependencies())
            .withKey(null)
            .withDescription(description);
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
            if (rowVals.size() == refs.size()+1)  {
                row.withValues(rowVals.toArray(new String[rowVals.size()]));
                rows.add(row);
            } else if (rowVals.size() == 1) {
                break;
            }
        }
        Body body = new Body()
            .withRows(rows.toArray(new Row[rows.size()]));
        Dataset dataset = new Dataset()
            .withDescriptor(descriptor)
            .withBody(body);
        return dataset;
    }
}
