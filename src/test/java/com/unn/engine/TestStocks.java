package com.unn.engine;

import com.unn.stocks.InputBatchPredictor;
import com.unn.stocks.InputMiner;
import org.junit.Test;

public class TestStocks {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    @Test
    public void testInputMiner_target1001() {
        InputMiner.runAll(String.format("%s/target-1001", folderPath), 1);
    }

    @Test
    public void testInputBatchPredictor_target1001() {
        new InputBatchPredictor(String.format("%s/target-1001", folderPath), "1001", false)
            .start();
    }

    @Test
    public void testMineOutputLayer() {
        InputMiner.runAll(String.format("%s/target-1001/output", folderPath), 2);
    }

    @Test
    public void testInputBatchRealtimePredictor_target1001() {
        new InputBatchPredictor(String.format("%s/target-1001", folderPath), "1001", true)
                .start();
    }

    @Test
    public void testOutputBatchRealtimePredictor_target1001() {
        new InputBatchPredictor(String.format("%s/target-1001/output", folderPath), "1", true)
                .start();
    }
}
