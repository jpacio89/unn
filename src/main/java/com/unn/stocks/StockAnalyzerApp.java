package com.unn.stocks;

import com.unn.common.utils.ZipHelper;
import com.unn.engine.Config;

import java.io.*;

public class StockAnalyzerApp {
    private static String basePath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    public static void main (String[] args) throws IOException, InterruptedException {
        basePath = String.format("%s/batch-mining", args[0].replaceFirst("^~", System.getProperty("user.home")));
        String targetInstrumentId = args[1];
        String uploadUrl = String.format("%s?name=target-%s", args[2], targetInstrumentId);
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

        System.out.println(String.format("|StockAnalyzerApp| Gathering layer 1 realtime outputs"));

        new InputBatchPredictor(folderPath, targetInstrumentId, true)
            .start();

        InputMiner.runAll(String.format("%s/output", folderPath), 2);

        System.out.println(String.format("|StockAnalyzerApp| Gathering layer 2 realtime outputs"));

        new InputBatchPredictor(String.format("%s/output", folderPath), "1", true)
            .start();

        System.out.println(String.format("|StockAnalyzerApp| Zipping deliverables"));

        String zipPath = String.format("%s/../target-%s.zip", folderPath, targetInstrumentId);
        ZipHelper.zip(folderPath, zipPath);

        System.out.println(String.format("|StockAnalyzerApp| Uploading deliverables"));

        uploadDeliverables(zipPath, uploadUrl);
    }

    private static void uploadDeliverables(String zipPath, String uploadUrl) throws IOException, InterruptedException {
        File zipFile = new File(zipPath);
        String[] command = new String[] {
            "curl", "-F", String.format("file=@%s", zipFile.getAbsolutePath()), uploadUrl
        };
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        for (String line; (line = reader.readLine()) != null;) {
            System.out.println(line);
        }

        p.waitFor();
        reader.close();
    }
}
