package com.unn.stocks;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.session.Session;

import java.io.File;

public class RealtimePredictor {
    private File inputFolder;

    public RealtimePredictor(File _inputFolder) {
        this.inputFolder = _inputFolder;
    }

    public RealtimePredictor run() {
        // TODO: endless loop routinely checks realtime.csv dataset and produces prediction
        OuterDataset outerDataset = loadRealtimeDataset();
        Session session;
        // session.
        return this;
    }

    private OuterDataset loadRealtimeDataset() {
        File dataset = new File(String.format("%s/realtime.csv", this.inputFolder.getAbsolutePath()));

        if (!dataset.exists()) {
            return null;
        }

        DatasetLocator locator = new FilesystemLocator(dataset.getAbsolutePath());
        FilesystemDatasetProvider provider = new FilesystemDatasetProvider(locator);
        OuterDataset outerDataset = provider.load();
        return outerDataset;
    }
}
