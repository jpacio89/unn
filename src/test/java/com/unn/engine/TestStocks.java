package com.unn.engine;

import com.unn.common.mining.MiningReport;
import com.unn.common.operations.AgentRole;
import com.unn.common.utils.Serializer;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TestStocks {
    private static String folderPath = "/Volumes/Legatron/data/serializations/stock-market-crawler/batch-mining";

    private Session mine(OuterDataset outerDataset, String target) {
        Context context = new Context();
        AgentRole role = new AgentRole();
        Session session = new Session(context, role);
        session.setOuterDataset(outerDataset);

        MineAction action = new MineAction();
        action.setSession(session);
        action.setConf(new JobConfig(target, new ArrayList<>()));

        action.act();

        MiningReport report = action.getSession().getReport();
        if (report == null || report.getConfusionMatrixes().size() == 0) {
            System.out.println("Report statistics --> null");
        } else {
            System.out.println(String.format("Report statistics -->\n%s", report.toString()));
        }

        return session;
    }

    private void writeReportToFile(File inputFolder, Session session) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(String.format("%s/report.log", inputFolder.getAbsolutePath())));
            writer.write(session.getReport().toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBatchMining_target1001() {
        String dataSourcePath = String.format("%s/target-1001", folderPath);
        File folder = new File(dataSourcePath);

        for (File inputFolder : folder.listFiles()) {
            if (!inputFolder.isDirectory() ||
                !inputFolder.getName().startsWith("input-")) {
                continue;
            }
            File sessionFile = new File(String.format("%s/predictor.v1.session", inputFolder.getAbsolutePath()));
            if (sessionFile.exists()) {
                System.out.println(String.format("|BatchMiner| Skipping %s", inputFolder.getName()));
                continue;
            }
            System.out.printf("|BatchMiner| Mining %s%n", inputFolder.getName());
            String datasetPath = String.format("%s/dataset.csv", inputFolder.getAbsolutePath());
            DatasetLocator locator = new FilesystemLocator(datasetPath);
            FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
            OuterDataset outerDataset = provider.load();
            Session session = mine(outerDataset, "outcome");
            Serializer.write(session, String.format("%s/predictor", inputFolder.getAbsolutePath()), "session");
            this.writeReportToFile(inputFolder, session);
        }
    }
}
