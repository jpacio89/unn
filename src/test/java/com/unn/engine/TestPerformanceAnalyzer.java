package com.unn.engine;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.engine.mining.PerformanceAnalyzer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestPerformanceAnalyzer {
    @Test
    public void testError() {
        PerformanceAnalyzer analyzer = new PerformanceAnalyzer();
        try {
            analyzer.addEvent(0, 10);
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void test1() throws Exception {
        PerformanceAnalyzer analyzer = new PerformanceAnalyzer();
        analyzer.addEvent(Config.STIM_MIN, Config.STIM_MAX);
        analyzer.addEvent(Config.STIM_MAX, Config.STIM_NULL);
        analyzer.addEvent(Config.STIM_MAX, Config.STIM_MAX);
        analyzer.addEvent(Config.STIM_MIN, Config.STIM_MIN);

        ConfusionMatrix matrix = analyzer.getConfusionMatrix();
        assertEquals(matrix.getUnknownRate(), 25);
        assertEquals(matrix.getTpr(), 50);
        assertEquals(matrix.getTnr(), 100);
        assertEquals(matrix.getAccuracy(), 66);
    }
}
