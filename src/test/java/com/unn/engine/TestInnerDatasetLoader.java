package com.unn.engine;

import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.NumericValuesDescriptor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TestInnerDatasetLoader {
    String[] getNumericParts(String suffix, String group) {
        return group
            .replace("numeric_", "")
            .replace(suffix, "")
            .split("_");
    }

    @Test
    public void test() {
        OuterDataset outerDataset = Datasets.dummy1();
        InnerDatasetLoader loader = new InnerDatasetLoader();
        loader.init(outerDataset);
        InnerDataset innerDataset = loader.load();
        ValueMapper mapper = loader.getValueMapper();

        Arrays.stream(new String[]{"x", "y", "z"}).forEach(feature -> {
            NumericValuesDescriptor descriptor = (NumericValuesDescriptor)
                mapper.getValuesDescriptorByFeature(feature);

            for (int i = 0; i < outerDataset.sampleCount(); ++i) {
                HashMap<String, String> sample = outerDataset.getSampleAsMap(i);
                int primer = Integer.parseInt(sample.get("primer"));
                String outerValue = sample.get(feature);
                ArrayList<String> activatedGroups = descriptor.getGroupByOuterValue(outerValue);

                activatedGroups.forEach(group -> {
                    IFunctor functor = descriptor.getFunctorByGroup(group);
                    int innerValue = innerDataset.getValueByTime(functor, primer);
                    assertEquals(innerValue, Config.STIM_MAX);
                });

                descriptor.getGroups().stream()
                    .filter(e -> !activatedGroups.contains(e))
                    .forEach(group -> {
                        IFunctor functor = descriptor.getFunctorByGroup(group);
                        int innerValue = innerDataset.getValueByTime(functor, primer);
                        assertEquals(innerValue, Config.STIM_MIN);
                    });
            }
        });

        // TODO: do checks
    }
}
