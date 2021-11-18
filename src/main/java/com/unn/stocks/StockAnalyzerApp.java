package com.unn.stocks;

public class StockAnalyzerApp {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    public static void main (String[] args) {
        String targetInstrumentId = args[0];

        System.out.println(String.format("|StockAnalyzerApp| Analyzing instrument %s", targetInstrumentId));
        System.out.println(String.format("|StockAnalyzerApp| Processing layer 1 inputs"));

        InputMiner.runAll(folderPath, targetInstrumentId);

        System.out.println(String.format("|StockAnalyzerApp| Predicting using layer 1 inputs"));

        if (!MiningHelper.hasOutputDataset(folderPath, targetInstrumentId)) {
            new InputBatchPredictor(folderPath, targetInstrumentId)
                .start();
        }

        System.out.println(String.format("|StockAnalyzerApp| Processing layer 2 inputs"));

        // TODO: check if session was already persisted
        MiningHelper.mineOutputLayer(folderPath, targetInstrumentId);
    }


}