package com.unn.stocks;

import com.unn.common.utils.ZipHelper;
import com.unn.engine.Config;

import java.io.*;

public class StockAnalyzerApp {
    private static String basePath = "/Volumes/Gondor/data/serializations/stock-market-crawler/batch-mining";

    public static void main (String[] args) throws IOException, InterruptedException {
        basePath = String.format("%s/batch-mining", args[0].replaceFirst("^~", System.getProperty("user.home")));
        String targetInstrumentId = args[1];
        String uploadUrl = String.format("%s?name=target-%s", args[2], targetInstrumentId);
        String folderPath = String.format("%s/target-%s", basePath, targetInstrumentId);
        boolean isRealtimeOnly = args.length == 4 && "--realtimeOnly".equals(args[3]);

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

        if (isRealtimeOnly) {
            System.out.println(String.format("|StockAnalyzerApp| Sending realtime prediction"));
            String realtimeCsv = String.format("%s/output/output/input-1/realtime.csv", folderPath);
            uploadDeliverables(realtimeCsv, String.format("%s.realtime.csv", uploadUrl));
        } else {
            System.out.println(String.format("|StockAnalyzerApp| Zipping deliverables"));

            String reportPath = String.format("%s/output/input-1/performance.v1.report", folderPath);
            String zipPath = String.format("%s/../target-%s.zip", folderPath, targetInstrumentId);
            ZipHelper.zip(folderPath, zipPath);

            System.out.println(String.format("|StockAnalyzerApp| Uploading deliverables"));

            uploadDeliverables(zipPath, String.format("%s.zip", uploadUrl));
            uploadDeliverables(reportPath, String.format("%s.v1.report", uploadUrl));
        }
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
