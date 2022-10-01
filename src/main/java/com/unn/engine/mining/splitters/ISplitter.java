package com.unn.engine.mining.splitters;

import com.unn.engine.interfaces.IFeature;
import com.unn.engine.utils.Pair;

import java.util.ArrayList;

public interface ISplitter {
    Pair<ArrayList<Integer>, ArrayList<Integer>> split(ArrayList<Integer> allTimes);
}
