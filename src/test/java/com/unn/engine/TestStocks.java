package com.unn.engine;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.stocks.InputBatchPredictor;
import com.unn.stocks.InputMiner;
import com.unn.stocks.MiningHelper;
import org.junit.Test;

public class TestStocks {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    @Test
    public void testBatchMining_target1001_layer2() {
        new InputBatchPredictor(folderPath, "1001")
            .start();
    }

    @Test
    public void testBatchMining_target1001() {
        InputMiner.runAll(folderPath, "1001");
    }

    @Test
    public void testMineStockLayer2() {
        String dataSourcePath = String.format("%s/target-1001/output/dataset.csv", folderPath);
        DatasetLocator locator = new FilesystemLocator(dataSourcePath);
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        MiningHelper.mine(outerDataset, "outcome");
    }
}
