package com.unn.engine.dataset;

import com.unn.common.dataset.*;
import com.unn.engine.Config;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.ScopeConfig;
import com.unn.engine.prediction.Prediction;

import java.util.*;

public class Datasets {

    private static Integer getRewardValue(OuterDataset dataset, ValueMapper mapper, String featureName, int sampleIndex, ScopeConfig config) {
        Integer featureIndex = dataset.getFeatureIndex(featureName);
        String outerValue = dataset.getFeatureAtSample(sampleIndex, featureIndex);
        ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(featureName);
        String featureGroup = valuesDescriptor.getGroupByOuterValue(outerValue, featureName);
        IFunctor func = valuesDescriptor.getFunctorByGroup(featureGroup);
        return func.equals(config.getFeatureSelector()) ?
            Config.STIM_MAX : Config.STIM_MIN;
    }

    public static InnerDataset toInnerDataset(OuterDataset dataset, ValueMapper mapper, ScopeConfig job) {
        // TODO: implement
        String timeFeatureName = "id"; // job.getTimeFeatureName();
        String rewardFeatureName = "Class"; // job.getRewardFeatureName();
        InnerDataset innerDataset = new InnerDataset();
        ArrayList<IFunctor> rawFunctors = InnerDatasetLoader.getFunctorsByFeatures(mapper);
        innerDataset.setFunctors(rawFunctors);

        for (int sampleIndex = 0; sampleIndex < dataset.sampleCount(); ++sampleIndex) {
            Integer timeFeatureIndex = dataset.getFeatureIndex(timeFeatureName);
            Integer outerTimeValue = Integer.parseInt(dataset.getFeatureAtSample(sampleIndex, timeFeatureIndex));
            Integer reward = getRewardValue(dataset, mapper, rewardFeatureName, sampleIndex, job);
            for (int i = 0; i < dataset.featureCount(); ++i) {
                String featureName = dataset.getHeader().get(i);
                String outerValue = dataset.getFeatureAtSample(sampleIndex, i);
                Integer innerValue = mapper.getInnerValue(featureName, outerValue);
                IFunctor identity = innerDataset.getFunctorByClassName(featureName);
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
            refs = Arrays.stream(refs).filter((ref) -> {
                return !Config.PRIMER.equals(ref) && !Config.ID.equals(ref);
            }).toArray(size -> new String[size]);
            for (String ref : refs) {
                ArrayList<Prediction> refPrediction = predictions.get(ref);
                if (i >= refPrediction.size()) {
                    break;
                }
                Prediction prediction = refPrediction.get(i);
                if (j == 0) {
                    rowVals.add(prediction.getTime().toString());
                }
                if (prediction.getValue() == null) {
                    rowVals.add("0");
                } else {
                    rowVals.add(prediction.getValue().toString());
                }
                j++;
            }
            if (rowVals.size() == refs.length+1)  {
                row.withValues(rowVals.toArray(new String[rowVals.size()]));
                rows.add(row);
            } else if (rowVals.size() == 0) {
                break;
            }
        }
        Body body = new Body()
            .withRows(rows.toArray(new Row[rows.size()]));
        return body;
    }
}
