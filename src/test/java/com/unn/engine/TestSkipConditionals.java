package com.unn.engine;

import com.unn.engine.skipconditionals.SkipConditionalRunner;
import com.unn.engine.utils.RandomManager;
import org.junit.Test;

public class TestSkipConditionals {

    @Test
    public void test1() {
        int[][] data = new int[1000][];
        String[] classes = new String[1000];

        for (int i = 0; i < data.length; ++i) {
            data[i] = new int[25*25];
            for (int j = 0; j < data[i].length; ++j) {
                data[i][j] = RandomManager.rand(0, 255);
                classes[i] = RandomManager.rand(0, 1) == 0 ? "A" : "B";
            }
        }

        SkipConditionalRunner runner = new SkipConditionalRunner();
        runner.init(10);
        runner.run(data, classes);
    }
}
