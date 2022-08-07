package com.unn.engine;

import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.metadata.*;
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

        innerDataset.getFunctors().forEach(functor -> {
            ArrayList<Integer> downTimes = innerDataset.getTimesByFunctor(functor, Config.get().STIM_MIN);
            ArrayList<Integer> upTimes = innerDataset.getTimesByFunctor(functor, Config.get().STIM_MAX);
            ArrayList<Integer> nullTimes = innerDataset.getTimesByFunctor(functor, Config.get().STIM_NULL);

            assertTrue( nullTimes.size() == 0);

            innerDataset.getTimes().forEach(time -> {
                Integer value = innerDataset.getValueByTime(functor, time);
                assertTrue(value == Config.get().STIM_MIN || value == Config.get().STIM_MAX);

                if (value == Config.get().STIM_MIN) {
                    assertTrue(downTimes.contains(time));
                    assertFalse(upTimes.contains(time));
                } else if (value == Config.get().STIM_MAX) {
                    assertFalse(downTimes.contains(time));
                    assertTrue(upTimes.contains(time));
                } else {
                    assertTrue(false);
                }
            });
        });

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
                ArrayList<String> activatedGroups = descriptor.getActivatedOutputFeatures(outerValue);

                activatedGroups.forEach(group -> {
                    IFeature functor = descriptor.getFeatureByName(group);
                    int innerValue = innerDataset.getValueByTime(functor, primer);
                    assertEquals(innerValue, Config.get().STIM_MAX);
                });

                descriptor.getOutputFeatures().stream()
                    .filter(e -> !activatedGroups.contains(e))
                    .forEach(group -> {
                        IFeature functor = descriptor.getFeatureByName(group);
                        int innerValue = innerDataset.getValueByTime(functor, primer);
                        assertEquals(innerValue, Config.get().STIM_MIN);
                    });
            }
        });

        // TODO: test all inner values either MIN or MAX
    }
}
