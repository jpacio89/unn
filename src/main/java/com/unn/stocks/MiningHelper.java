package com.unn.stocks;

import com.unn.common.mining.MiningReport;
import com.unn.common.operations.AgentRole;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import com.unn.engine.session.actions.MineAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MiningHelper {
    public static Session mine(OuterDataset outerDataset, String target, int layer) {
        Context context = new Context();
        AgentRole role = new AgentRole();

        Session session = new Session(context, role);
        session.setOuterDataset(outerDataset);

        MineAction action = new MineAction();
        action.setSession(session);
        action.setConf(new JobConfig(target, new ArrayList<>(), layer));

        action.act();

        MiningReport report = action.getSession().getReport();

        if (report == null || report.getConfusionMatrixes().size() == 0) {
            System.out.println("No reports to showcase");
        } else {
            System.out.println(String.format("Report statistics:%n%s", report.toString()));
        }

        return session;
    }

    public static void mineOutputLayer(String folderPath, String targetInstrumentId) {
        File sessionFile = new File(String.format("%s/target-%s/output/predictor.v1.session",
            folderPath, targetInstrumentId));

        if (sessionFile.exists()) {
            System.out.println(String.format("Skipping %s", sessionFile.getAbsolutePath()));
            // TODO: load serialized file
            return;
        }

        String dataSourcePath = String.format("%s/target-%s/output/dataset.csv",
                folderPath, targetInstrumentId);
        DatasetLocator locator = new FilesystemLocator(dataSourcePath);
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        Session session = MiningHelper.mine(outerDataset, "outcome", 2);

        //MiningHelper.writeReportToFile(this.inputFolder, session);
    }

    public static void writeReportToFile(File inputFolder, Session session) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(
                    String.format("%s/report.log", inputFolder.getAbsolutePath())));
            writer.write(session.getReport().toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasOutputDataset(String folderPath, String targetInstrumentId) {
        String dataSourcePath = String.format("%s/target-%s/output", folderPath, targetInstrumentId);
        File folder = new File(dataSourcePath);

        if (!folder.exists()) {
            folder.mkdirs();
            return false;
        }

        File outputDataset = new File(String.format("%s/dataset.csv", folder.getAbsolutePath()));
        return outputDataset.exists();
    }
}
