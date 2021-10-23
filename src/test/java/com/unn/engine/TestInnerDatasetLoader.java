package com.unn.engine;

import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.metadata.*;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TestInnerDatasetLoader {
    @Test
    public void test() {
        OuterDataset outerDataset = Datasets.dummy4();
        InnerDatasetLoader loader = new InnerDatasetLoader();
        loader.init(outerDataset);
        InnerDataset innerDataset = loader.load();
        ValueMapper mapper = loader.getValueMapper();

        Arrays.stream(new String[]{"x", "y", "z", "reward"}).forEach(feature -> {
            ValuesDescriptor descriptor = mapper.getValuesDescriptorByFeature(feature);

            if ("reward".equals(feature)) {
                assertTrue(descriptor instanceof DiscreteValuesDescriptor);
            } else if ("y".equals(feature)) {
                assertTrue(descriptor instanceof MixedValuesDescriptor);
            } else {
                assertTrue(descriptor instanceof NumericValuesDescriptor);
            }

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

        // TODO: test all inner values either MIN or MAX
    }
}
