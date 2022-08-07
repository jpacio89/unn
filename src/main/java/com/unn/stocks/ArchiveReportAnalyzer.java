package com.unn.stocks;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.common.mining.MiningReport;
import com.unn.common.utils.Serializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ArchiveReportAnalyzer {
    private static String archivePath = "/Volumes/Gondor/data/serializations/stock-market-crawler/targets-archive";

    public static void main(String[] args) {
        File archive = new File(archivePath);
        File[] files = archive.listFiles((File dir, String name) -> name.endsWith(".report"));

        for (File reportFile : files) {
            MiningReport report = (MiningReport) Serializer.read(reportFile.getAbsolutePath().replace(".v1.report", ""), "report");
            if (!ofInterest(report)) {
                continue;
            }
            System.out.printf("%s,%n", reportFile.getName());
            //System.out.println(report.toString());
        }
    }

    private static boolean ofInterest(MiningReport report) {
        ArrayList<String> keys = report.confusionMatrixes.keySet().stream()
            //.filter(key -> key.contains("Over:"))
            .filter(key -> key.contains("Below:"))
            .collect(Collectors.toCollection(ArrayList::new));
        if (keys.size() == 0) {
            return false;
        }
        String aboveKey = keys.get(0);
        ConfusionMatrix matrix = report.getConfusionMatrixes().get(aboveKey);
        return matrix.getTpCount() > 50 && matrix.getTpr() > 80;
    }
}
