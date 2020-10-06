package com.unn.engine.mining;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.interfaces.IFunctor;

import java.util.ArrayList;

public class ScopeConfig {
    IFunctor featureSelector;
    InnerDatasetLoader loader;
    InnerDataset dataset;
    ArrayList<IFunctor> noMiningGroups;

    public ScopeConfig(InnerDatasetLoader loader, InnerDataset innerDataset,
        IFunctor featureSelector, ArrayList<IFunctor> rewardGroups) {
        this.featureSelector = featureSelector;
        this.loader = loader;
        this.dataset = innerDataset;
        this.noMiningGroups = rewardGroups;
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

    public ArrayList<IFunctor> getNoMiningGroups() {
        return noMiningGroups;
    }
}
