package com.unn.stocks;

import java.io.File;

public class RealtimePredictor {
    private File inputFolder;

    public RealtimePredictor(File _inputFolder) {
        this.inputFolder = _inputFolder;
    }

    public RealtimePredictor run() {
        // TODO: endless loop routinely checks realtime.csv dataset and produces prediction
        return this;
    }
}
