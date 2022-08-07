package com.unn.engine;

import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.mining.PredicateFactory;
import com.unn.engine.mining.models.Predicate;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPredicateFactory {
    private long countFalseConditions(PredicateFactory factory, Predicate predicate, int time) {
        return predicate.conditions.stream()
            .filter(condition -> !factory.checkTime(condition.feature, time, condition.activationValue))
            .count();
    }

    private InnerDataset getDataset() {
        OuterDataset outerDataset = Datasets.dummy3();
        InnerDatasetLoader loader = new InnerDatasetLoader();
        loader.init(outerDataset);
        InnerDataset innerDataset = loader.load();
        return innerDataset;
    }

    @Test
    public void test() {
        InnerDataset innerDataset = this.getDataset();

        ArrayList<IFeature> features = innerDataset.getFunctors();
        IFeature target = features.get(0);

        PredicateFactory factory = new PredicateFactory(innerDataset, Config.get().STIM_MAX, null);
        factory.init(features);

        ArrayList<Integer> times = innerDataset.getTimes();

        ArrayList<Integer> timesLow  = innerDataset.getTimesByFunctor(
                target, Config.get().STIM_MIN, times);
        ArrayList<Integer> timesHigh = innerDataset.getTimesByFunctor(
                target, Config.get().STIM_MAX, times);

        for (int i = 0; i < 100; ++i) {
            try {
                Predicate predicate = factory.randomPredicate(timesHigh, timesLow);

                if (predicate == null) {
                    continue;
                }

                assertTrue(predicate.targetTimes.size() > 0);

                timesLow.forEach(
                    time -> assertTrue(countFalseConditions(factory, predicate, time) > 0));

                predicate.targetTimes.forEach(
                    time -> assertTrue(countFalseConditions(factory, predicate, time) == 0));
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }
    }
}
