package com.unn.stocks;

import com.unn.common.mining.MiningReport;
import com.unn.common.utils.Serializer;
import com.unn.engine.session.Session;

import java.io.File;
import java.io.IOException;

public class ArchiveReportAnalyzer {
    private static String archivePath = "/Volumes/Legatron/data/serializations/stock-market-crawler/targets-archive";

    public static void main(String[] args) {
        File archive = new File(archivePath);
        File[] files = archive.listFiles((File dir, String name) -> name.endsWith(".report"));

        for (File reportFile : files) {
            MiningReport report = (MiningReport) Serializer.read(reportFile.getAbsolutePath().replace(".v1.report", ""), "report");
            System.out.println(reportFile.getAbsolutePath());
            System.out.println(report.toString());
        }

    }
}
