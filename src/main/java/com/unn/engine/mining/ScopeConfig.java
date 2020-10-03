package com.unn.engine.mining;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.interfaces.IFunctor;

public class ScopeConfig {
    IFunctor featureSelector;
    InnerDatasetLoader loader;
    InnerDataset dataset;

    public ScopeConfig(InnerDatasetLoader loader, InnerDataset innerDataset, IFunctor featureSelector) {
        this.featureSelector = featureSelector;
        this.loader = loader;
        this.dataset = innerDataset;
    }

    public IFunctor getFeatureSelector() {
        return featureSelector;
    }

    public void setFeatureSelector(IFunctor featureSelector) {
        this.featureSelector = featureSelector;
    }

    public InnerDatasetLoader getLoader() {
        return loader;
    }

    public void setLoader(InnerDatasetLoader loader) {
        this.loader = loader;
    }

    public InnerDataset getInnerDataset() {
        return this.dataset;
    }
}
