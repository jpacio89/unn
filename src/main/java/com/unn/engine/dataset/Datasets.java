package com.unn.engine.dataset;

import com.unn.common.dataset.*;
import com.unn.engine.Config;
import com.unn.engine.functions.ValueTime;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.prediction.Prediction;

import java.util.*;

public class Datasets {

    public static InnerDataset toInnerDataset(OuterDataset dataset, ValueMapper mapper) {
        // TODO: implement
        String timeFeatureName = "primer"; // job.getTimeFeatureName();
        InnerDataset innerDataset = new InnerDataset();
        ArrayList<IFunctor> rawFunctors = InnerDatasetLoader.getFunctorsByFeatures(mapper);
        innerDataset.setFunctors(rawFunctors);

        for (int sampleIndex = 0; sampleIndex < dataset.sampleCount(); ++sampleIndex) {
            Integer timeFeatureIndex = dataset.getFeatureIndex(timeFeatureName);
            Integer outerTimeValue = sampleIndex;
            if (timeFeatureIndex > -1) {
                outerTimeValue = Integer.parseInt(dataset.getFeatureAtSample(sampleIndex, timeFeatureIndex));
            }

            for (int i = 0; i < dataset.featureCount(); ++i) {
                String featureName = dataset.getHeader().get(i);
                String outerValue = dataset.getFeatureAtSample(sampleIndex, i);
                ValuesDescriptor descriptor = mapper.getValuesDescriptorByFeature(featureName);
                if (descriptor == null) {
                    continue;
                }
                ArrayList<String> targetGroups = descriptor.getGroupByOuterValue(outerValue);
                for (String group : descriptor.getGroups()) {
                    Integer value = targetGroups.contains(group) ?
                        Config.STIM_MAX : Config.STIM_MIN;
                    IFunctor func = descriptor.getFunctorByGroup(group);
                    ValueTime vtr = new ValueTime(func, value, outerTimeValue);
                    innerDataset.add(vtr);
                }
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
            refs = Arrays.stream(refs).filter((ref) ->
                !Config.PRIMER.equals(ref) && !Config.ID.equals(ref)).toArray(size -> new String[size]);
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
            if (rowVals.size() == 0) {
                break;
            } else if (rowVals.size() >= refs.length + 1)  {
                row.withValues(rowVals.toArray(new String[rowVals.size()]));
                rows.add(row);
            }
        }
        Body body = new Body()
            .withRows(rows.toArray(new Row[rows.size()]));
        return body;
    }
}
