package com.unn.engine.mining.splitters;

import com.unn.engine.utils.Pair;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class HashSplitter implements ISplitter {
    int layer;

    public HashSplitter(int _layer) {
        this.layer = _layer;
    }

    @Override
    public Pair<ArrayList<Integer>, ArrayList<Integer>> split(ArrayList<Integer> allTimes) {
        ArrayList<Integer> trainTimeSets = allTimes.stream()
                .filter(time -> getGroup(time) == 0)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> testTimeSets = allTimes.stream()
                .filter(time -> getGroup(time) == 1)
                .collect(Collectors.toCollection(ArrayList::new));
        return new Pair<>(trainTimeSets, testTimeSets);
    }

    private Integer getGroup(int time) {
        int hashcode = Integer.toString(time).hashCode();

        for (int i = 0; i < this.layer - 1; ++i) {
            if (hashcode % 2 == 0) {
                return -1;
            }
            hashcode /= 2;
        }

        return hashcode % 2 == 0 ? 0 : 1;
    }
}
