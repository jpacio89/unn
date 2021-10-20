package com.unn.engine;

import com.unn.engine.dataset.OuterDataset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOuterDataset {
    @Test
    public void test() {
        String[] features = { "id", "primer", "x", "y", "z", "reward" };
        String[][] samples = {
            { "1", "1001", "0.1401828586", "0.2397944053", "0.3035742177", "A" },
            { "2", "1002", "0.7159866866", "0.3563118436", "0.7011239919", "B" },
            { "3", "1003", "0.5239163291", "0.8441958543", "0.9663912179", "C" },
            { "4", "1004", "0.4114926047", "0.4013922481", "0.6298981341", "D" },
            { "5", "1005", "0.4071991520", "0.8481527360", "0.8176264850", "E" }
        };

        OuterDataset dataset = new OuterDataset();
        dataset.setHeader(features);

        for (int i = 0; i < samples.length; ++i) {
            dataset.addSample(samples[i]);
        }

        assertEquals(dataset.featureCount(), 6);
        assertEquals(dataset.sampleCount(), 5);
        assertEquals(dataset.getSampleAsMap(2).get("id"), "3");
        assertEquals(dataset.getSampleAsMap(4).get("x"), "0.4071991520");
        assertEquals(dataset.getFeatureIndex("z"), 4);
        assertEquals(dataset.getSample(0).get(1), "1001");
        assertEquals(dataset.getFeatureAtSample(1, 5), "B");
    }
}
