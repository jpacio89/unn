package com.unn.engine;

import com.unn.engine.data.Datasets;
import com.unn.engine.dataset.OuterDataset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOuterDataset {
    @Test
    public void test() {
        OuterDataset dataset = Datasets.dummy1();
        assertEquals(dataset.featureCount(), 6);
        assertEquals(dataset.sampleCount(), 5);
        assertEquals(dataset.getSampleAsMap(2).get("id"), "3");
        assertEquals(dataset.getSampleAsMap(4).get("x"), "0.40719");
        assertEquals(dataset.getFeatureIndex("z"), 4);
        assertEquals(dataset.getSample(0).get(1), "1001");
        assertEquals(dataset.getFeatureAtSample(1, 5), "B");
    }
}
