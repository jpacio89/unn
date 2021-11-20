package com.unn.engine;

import com.unn.stocks.InputBatchPredictor;
import com.unn.stocks.InputMiner;
import com.unn.stocks.MiningHelper;
import org.junit.Test;

public class TestStocks {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    @Test
    public void testInputBatchPredictor_target1001() {
        new InputBatchPredictor(folderPath, "1001")
            .start();
    }

    @Test
    public void testInputMiner_target1001() {
        InputMiner.runAll(folderPath, "1001");
    }

    /*@Test
    public void testSerializer() {
        Session session = (Session) Serializer.read(String.format("%s/target-1001/input-1024/predictor", folderPath), "session");
        Serializer.write(session, String.format("%s/target-1001/input-1024/test", folderPath), "ser");
        Session session2 = (Session) Serializer.read(String.format("%s/target-1001/input-1024/test", folderPath), "ser");

        //session.getInnerDatasetLoader().reconstruct();
        session2.getInnerDatasetLoader().reconstruct();
        session2.getScopes().values().forEach(
            scope -> assertTrue(scope.getModel().getInnerDataset() == session2.getInnerDatasetLoader().getInitialInnerDataset()));
    }*/

    @Test
    public void testMineOutputLayer() {
        MiningHelper.mineOutputLayer(folderPath, "1001");
    }
}
