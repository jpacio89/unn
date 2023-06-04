package com.unn.engine.mining.splitters;

import com.unn.engine.utils.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SimpleSplitter implements ISplitter {
    double dividingRatio;

    public SimpleSplitter(double _dividingRatio) {
        this.dividingRatio = _dividingRatio;
    }

    @Override
    public Pair<ArrayList<Integer>, ArrayList<Integer>> split(ArrayList<Integer> allTimes) {
        allTimes.sort(Comparator.comparingInt(a -> a));
        int trainSize = (int) Math.round(allTimes.size() * this.dividingRatio);
        ArrayList<Integer> trainTimes = allTimes.stream().limit(trainSize)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> testTimes = allTimes.stream().skip(trainSize)
                .collect(Collectors.toCollection(ArrayList::new));
        return new Pair<>(trainTimes, testTimes);
    }
}
