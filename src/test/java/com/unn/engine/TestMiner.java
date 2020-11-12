package com.unn.engine;

import com.unn.engine.mining.MinerXGBoost;
import org.junit.Test;

public class TestMiner {

    @Test
    public void testXGBoost() {
        MinerXGBoost miner = new MinerXGBoost();
        miner.mine();
    }
}
