package com.unn.engine.mining.models;

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
    ArrayList<Integer> trainTimes;
    ArrayList<Integer> testTimes;

    public ScopeConfig(InnerDatasetLoader loader, InnerDataset innerDataset,
        String outerFeature, IFunctor featureSelector, ArrayList<IFunctor> rewardGroups,
        ArrayList<Integer> trainTimes, ArrayList<Integer> testTimes) {
        this.innerFeature = featureSelector;
        this.loader = loader;
        this.dataset = innerDataset;
        this.noMiningGroups = rewardGroups;
        this.outerFeature = outerFeature;
        this.trainTimes = trainTimes;
        this.testTimes = testTimes;
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

    public ArrayList<Integer> getTrainTimes() {
        return trainTimes;
    }

    public void setTrainTimes(ArrayList<Integer> trainTimes) {
        this.trainTimes = trainTimes;
    }

    public ArrayList<Integer> getTestTimes() {
        return testTimes;
    }

    public void setTestTimes(ArrayList<Integer> testTimes) {
        this.testTimes = testTimes;
    }
}
