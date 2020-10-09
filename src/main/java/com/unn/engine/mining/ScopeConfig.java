package com.unn.engine.mining;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.interfaces.IFunctor;

import java.util.ArrayList;

public class ScopeConfig {
    String outerFeature;
    IFunctor innerFeature;
    InnerDatasetLoader loader;
    InnerDataset dataset;
    ArrayList<IFunctor> noMiningGroups;

    public ScopeConfig(InnerDatasetLoader loader, InnerDataset innerDataset,
        String outerFeature, IFunctor featureSelector, ArrayList<IFunctor> rewardGroups) {
        this.innerFeature = featureSelector;
        this.loader = loader;
        this.dataset = innerDataset;
        this.noMiningGroups = rewardGroups;
        this.outerFeature = outerFeature;
    }

    public IFunctor getInnerFeature() {
        return innerFeature;
    }

    public void setInnerFeature(IFunctor innerFeature) {
        this.innerFeature = innerFeature;
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

    public String getOuterFeature() {
        return outerFeature;
    }

    public void setOuterFeature(String outerFeature) {
        this.outerFeature = outerFeature;
    }
}
