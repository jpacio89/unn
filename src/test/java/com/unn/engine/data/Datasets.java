package com.unn.engine.data;

import com.unn.engine.dataset.OuterDataset;

public class Datasets {

    public static OuterDataset dummy1() {
        String[] features = { "id", "primer", "x", "y", "z", "reward" };
        String[][] samples = {
                { "1", "1001", "0.14018", "0.23979", "0.30357", "A" },
                { "2", "1002", "0.71598", "0.35631", "0.70112", "B" },
                { "3", "1003", "0.52391", "0.84419", "0.96639", "C" },
                { "4", "1004", "0.41149", "0.40139", "0.62989", "D" },
                { "5", "1005", "0.40719", "0.84815", "0.81762", "E" }
        };

        OuterDataset dataset = new OuterDataset();
        dataset.setHeader(features);

        for (int i = 0; i < samples.length; ++i) {
            dataset.addSample(samples[i]);
        }

        return dataset;
    }
}
