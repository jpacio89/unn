package com.unn.engine.mining.models;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.dataset.InnerDatasetLoader;
import com.unn.engine.interfaces.IFeature;

import java.io.Serializable;
import java.util.ArrayList;

public class ScopeConfig implements Serializable {
    String outerFeature;
    IFeature innerFeature;
    ArrayList<IFeature> noMiningGroups;
    ArrayList<Integer> trainTimes;
    ArrayList<Integer> testTimes;

    public ScopeConfig(String outerFeature, IFeature featureSelector, ArrayList<IFeature> rewardGroups,
                       ArrayList<Integer> trainTimes, ArrayList<Integer> testTimes) {
        this.innerFeature = featureSelector;
        this.noMiningGroups = rewardGroups;
        this.outerFeature = outerFeature;
        this.trainTimes = trainTimes;
        this.testTimes = testTimes;
    }

    public IFeature getInnerFeature() {
        return innerFeature;
    }

    public void setInnerFeature(IFeature innerFeature) {
        this.innerFeature = innerFeature;
    }

    public ArrayList<IFeature> getNoMiningGroups() {
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
