package com.unn.engine.dataset;

import com.unn.common.dataset.Dataset;
import com.unn.common.dataset.Row;
import com.unn.engine.metadata.ValueMapper;

import java.util.ArrayList;
import java.util.Arrays;

public class Datasets {

    public static InnerDataset toInnerDataset(OuterDataset dataset, ValueMapper mapper) {
        // TODO: implement
        return null;
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
