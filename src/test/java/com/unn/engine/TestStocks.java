package com.unn.engine;

import com.unn.stocks.InputBatchPredictor;
import com.unn.stocks.InputMiner;
import com.unn.stocks.MiningHelper;
import org.junit.Test;

public class TestStocks {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    @Test
    public void testInputMiner_target1001() {
        InputMiner.runAll(folderPath, "1001");
    }

    @Test
    public void testInputBatchPredictor_target1001() {
        new InputBatchPredictor(folderPath, "1001")
            .start();
    }

    @Test
    public void testMineOutputLayer() {
        MiningHelper.mineOutputLayer(folderPath, "1001");
    }
}
