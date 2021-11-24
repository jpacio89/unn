package com.unn.stocks;

import com.unn.engine.Config;

public class StockAnalyzerApp {
    private static String basePath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    public static void main (String[] args) {
        basePath = args[0];
        String targetInstrumentId = args[1];
        String folderPath = String.format("%s/target-%s", basePath, targetInstrumentId);

        System.out.println(String.format("|StockAnalyzerApp| Analyzing instrument %s", targetInstrumentId));
        System.out.println(String.format("|StockAnalyzerApp| Processing layer 1 inputs"));

        Config.get().MODEL_PREDICTION_PREDICATE_HIT_COUNT = 1;

        InputMiner.runAll(folderPath, 1);

        System.out.println(String.format("|StockAnalyzerApp| Predicting using layer 1 inputs"));

        if (!MiningHelper.hasOutputDataset(folderPath)) {
            new InputBatchPredictor(folderPath, targetInstrumentId, false)
                .start();
        }

        System.out.println(String.format("|StockAnalyzerApp| Processing layer 2 inputs"));

        Config.get().MODEL_PREDICTION_PREDICATE_HIT_COUNT = 30;

        InputMiner.runAll(String.format("%s/output", folderPath), 2);

        System.out.println(String.format("|StockAnalyzerApp| Gathering layer 1 realtime outputs"));

        new InputBatchPredictor(folderPath, targetInstrumentId, true)
            .start();

        System.out.println(String.format("|StockAnalyzerApp| Gathering layer 2 realtime outputs"));

        new InputBatchPredictor(String.format("%s/output", folderPath), "1", true)
            .start();

    }


}
