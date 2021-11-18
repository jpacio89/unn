package com.unn.stocks;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

// TODO: set layer
public class InputMiner {
    private final String outcomeFeature = "outcome";
    private File inputFolder;

    public InputMiner(File _inputFolder) {
        this.inputFolder = _inputFolder;
    }

    public void start() {
        File sessionFile = new File(String.format("%s/predictor.v1.session", this.inputFolder.getAbsolutePath()));

        if (sessionFile.exists()) {
            System.out.println(String.format("|BatchMiner| Skipping %s", this.inputFolder.getName()));
            return;
        }

        System.out.printf("|BatchMiner| Mining %s%n", this.inputFolder.getName());

        String datasetPath = String.format("%s/dataset.csv", this.inputFolder.getAbsolutePath());
        DatasetLocator locator = new FilesystemLocator(datasetPath);
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();

        Session session = MiningHelper.mine(outerDataset, outcomeFeature);
        Serializer.write(session, String.format("%s/predictor", this.inputFolder.getAbsolutePath()), "session");

        MiningHelper.writeReportToFile(this.inputFolder, session);
    }

    public static void runAll(String folderPath, String targetInstrumentId) {
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

}
