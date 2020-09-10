package com.unn.engine.dataset;

import com.unn.common.dataset.Dataset;
import com.unn.common.dataset.Row;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.metadata.ValueMapper;

import java.util.ArrayList;
import java.util.Arrays;

public class Datasets {

    public static InnerDataset toInnerDataset(OuterDataset dataset, ValueMapper mapper) {
        InnerDataset innerDataset = new InnerDataset();
        // TODO: implement
        // innerDataset.setTrainingLeaves(getOperators(mapper.getFeatures(), this.config.targetFeature, false));
        // innerDataset.setAllLeaves(leaves);
        for (int sampleIndex = 0; sampleIndex < dataset.sampleCount(); ++sampleIndex) {
            for (int i = 0; i < dataset.featureCount(); ++i) {
                String featureName = dataset.getHeader().get(i);
                String outerValue = dataset.getFeatureAtSample(sampleIndex, i);
                Integer innerValue = mapper.getInnerValue(featureName, outerValue);
                // TODO: add class, time and reward
                ValueTimeReward vtr = new ValueTimeReward(null, innerValue, null, null);
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
