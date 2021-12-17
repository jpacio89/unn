package com.unn.stocks;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.common.mining.MiningReport;
import com.unn.common.utils.CSVHelper;
import com.unn.common.utils.Serializer;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.OuterDatasetLoader;
import com.unn.engine.dataset.filesystem.FilesystemLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CloudPredictionsAnalyzer {
    private static String archivePath = "/Volumes/Legatron/data/serializations/stock-market-crawler/targets-archive";

    public static void main(String[] args) {
        File archive = new File(archivePath);
        File[] files = archive.listFiles((File dir, String name) -> name.endsWith(".realtime.csv"));

        for (File datasetFile : files) {
            try {
                OuterDataset outerDataset = new OuterDatasetLoader().load(new FilesystemLocator(datasetFile.getAbsolutePath()));
                for (String feature : outerDataset.getHeader()) {
                    if (feature.contains("Over:")) {
                        String value = outerDataset.getSampleAsMap(0).get(feature);
                        if (value == null || value.equals("F") || value.equals("N")) {
                            continue;
                        }
                        if (value.equals("T") || Double.parseDouble(value) >= 2.0) {
                            System.out.printf("#%s -> %s%n", datasetFile.getName()
                                .replace("target-", "")
                                .replace(".realtime.csv", ""), value);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
