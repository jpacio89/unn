package com.unn.engine;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.session.Context;
import org.junit.Test;

public class TestInnerDatasetLoader {
    // InnerDatasetLoader is a key component in the engine
    // as it is responsible for transforming any inputted dataset (OuterDataset)
    // into a dataset (InnerDataset) that can be digested by the engine:
    //   - converting labels into numerics
    //   - clustering numerics
    //   - mapping mixed values (labels and numerics)
    private OuterDataset getOuterDataset() {
        // TODO: get outer dataset
        return null;
    }

    private Context getContext() {
        // TODO: get context
        return null;
    }

    private JobConfig getJob() {
        // TODO: get job
        return null;
    }

    @Test
    public void testBasicFlow() {
        OuterDataset outerDataset = getOuterDataset();
        Context context = getContext();
        JobConfig job = getJob();
        InnerDatasetLoader loader = new InnerDatasetLoader();
        loader.init(null, job, outerDataset);
        InnerDataset innerDataset = loader.load();
        // TODO: do checks
    }

    @Test
    public void testReuseValueMapper() {
        // TODO: get outer dataset
        // TODO: create InnerDatasetLoader instance
        // TODO: convert
        // TODO: get ValueMapper
        // TODO: convert second dataset using value mapper
        //  (add unknown outer values not found in dataset 1)
    }

    // TODO: test discrete mapping
    // TODO: test numeric mapping
    // TODO: test mixed mapping
    // TODO: test feature binarization
}
