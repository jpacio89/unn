package com.unn.stocks;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.session.Session;

import java.io.File;

public class StockAnalyzerApp {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    public static void main (String[] args) {
        String targetInstrumentId = args[0];

        System.out.println(String.format("|StockAnalyzerApp| Analyzing instrument %s", targetInstrumentId));
        System.out.println(String.format("|StockAnalyzerApp| Processing layer 1 inputs"));

        InputMiner.runAll(folderPath, targetInstrumentId);
        //processInputs(targetInstrumentId);

        InputBatchPredictor batchPredictor = new InputBatchPredictor(folderPath, targetInstrumentId);
        batchPredictor.start();

        mineNextLayer(targetInstrumentId);
    }

    private static void processInputs(String targetInstrumentId) {


        String dataSourcePath = String.format("%s/target-%s", folderPath, targetInstrumentId);
        File folder = new File(dataSourcePath);

        for (File inputFolder : folder.listFiles()) {
            if (!inputFolder.isDirectory() ||
                    !inputFolder.getName().startsWith("input-")) {
                continue;
            }
            InputMiner inputMiner = new InputMiner(inputFolder);
            inputMiner.start();
        }
    }

    private static void mineNextLayer(String targetInstrumentId) {
        System.out.println(String.format("|StockAnalyzerApp| Processing layer 2 inputs"));

        String dataSourcePath = String.format("%s/target-%s/output/dataset.csv",
            folderPath, targetInstrumentId);
        DatasetLocator locator = new FilesystemLocator(dataSourcePath);
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        Session session = MiningHelper.mine(outerDataset, "outcome");

        //MiningHelper.writeReportToFile(this.inputFolder, session);
    }
}
