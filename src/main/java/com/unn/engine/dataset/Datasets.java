package com.unn.engine.dataset;

import com.unn.common.dataset.Dataset;
import com.unn.common.dataset.Row;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.JobConfig;

import java.util.ArrayList;
import java.util.Arrays;

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
        // TODO: implement
        // innerDataset.setTrainingLeaves(getOperators(mapper.getFeatures(), this.config.targetFeature, false));
        // innerDataset.setAllLeaves(leaves);
        for (int sampleIndex = 0; sampleIndex < dataset.sampleCount(); ++sampleIndex) {
            Integer timeFeatureIndex = dataset.getFeatureIndex(timeFeatureName);
            Integer outerTimeValue = Integer.parseInt(dataset.getFeatureAtSample(sampleIndex, timeFeatureIndex));
            Integer reward = getRewardValue(dataset, mapper, rewardFeatureName, sampleIndex, job);
            for (int i = 0; i < dataset.featureCount(); ++i) {
                String featureName = dataset.getHeader().get(i);
                String outerValue = dataset.getFeatureAtSample(sampleIndex, i);
                Integer innerValue = mapper.getInnerValue(featureName, outerValue);
                // TODO: add class
                ValueTimeReward vtr = new ValueTimeReward(
                null,
                    innerValue,
                    outerTimeValue,
                    reward
                );
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
}
