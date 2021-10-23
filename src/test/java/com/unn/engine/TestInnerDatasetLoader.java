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

        innerDataset.getFunctors().forEach(functor -> {
            ArrayList<Integer> downTimes = innerDataset.getTimesByFunctor(functor, Config.STIM_MIN);
            ArrayList<Integer> upTimes = innerDataset.getTimesByFunctor(functor, Config.STIM_MAX);
            ArrayList<Integer> nullTimes = innerDataset.getTimesByFunctor(functor, Config.STIM_NULL);

            assertTrue(nullTimes == null || nullTimes.size() == 0);

            innerDataset.getTimes().forEach(time -> {
                Integer value = innerDataset.getValueByTime(functor, time);
                assertTrue(value == Config.STIM_MIN || value == Config.STIM_MAX);

                if (value == Config.STIM_MIN) {
                    assertTrue(downTimes.contains(time));
                    assertFalse(upTimes.contains(time));
                } else if (value == Config.STIM_MAX) {
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
