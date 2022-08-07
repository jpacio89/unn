package com.unn.engine;

import com.unn.stocks.InputBatchPredictor;
import com.unn.stocks.InputMiner;
import org.junit.Test;

public class TestStocks {
    private String instrumentId = "1001";
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    @Test
    public void testInputMiner_target() {
        InputMiner.runAll(String.format("%s/target-%s", folderPath, instrumentId), 1);
    }

    @Test
    public void testInputBatchPredictor_target() {
        new InputBatchPredictor(String.format("%s/target-%s", folderPath, instrumentId), instrumentId, false)
            .start();
    }

    @Test
    public void testMineOutputLayer() {
        InputMiner.runAll(String.format("%s/target-%s/output", folderPath, instrumentId), 2);
    }

    @Test
    public void testInputBatchRealtimePredictor_target() {
        new InputBatchPredictor(String.format("%s/target-%s", folderPath, instrumentId), instrumentId, true)
                .start();
    }

    @Test
    public void testOutputBatchRealtimePredictor_target1001() {
        new InputBatchPredictor(String.format("%s/target-%s/output", folderPath, instrumentId), "1", true)
                .start();
    }
}
