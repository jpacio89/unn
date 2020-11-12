package com.unn.engine.mining;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.util.HashMap;
import java.util.Map;

public class MinerXGBoost {

    public MinerXGBoost() { }

    public void mine() {
        try {
            Map<String, Object> params = new HashMap<String, Object>() {
                {
//                    put("eta", 1);
                    put("max_depth", 2);
//                    put("objective", "reg:squarederror");
 //                   put("eval_metric", "rmse");
                }
            };

            float[] labels = new float[] { 2f, 6f, 20f, 100f };
            float[] labels2 = new float[] { 8f, 40f, 14f, 100f };

            float[] data = new float[] { 1f, 3f, 10f, 50f };
            int nrow = 4;
            int ncol = 1;
            float missing = 0.0f;
            DMatrix trainMat = new DMatrix(data, nrow, ncol, missing);
            trainMat.setLabel(labels);
            int nround = 2;

            float[] data2 = new float[] { 4f, 20f, 7f, 50f };
            int nrow2 = 4;
            int ncol2 = 1;
            float missing2 = 0.0f;
            DMatrix testMat = new DMatrix(data, nrow2, ncol2, missing2);
            testMat.setLabel(labels);

            Map<String, DMatrix> watches = new HashMap<String, DMatrix>() {
                {
                    put("train", trainMat);
                    put("test", testMat);
                }
            };

            Booster booster = XGBoost.train(trainMat, params, nround, watches, null, null);

            // predict
            float[][] predicts = booster.predict(testMat);
            // predict leaf
            float[][] leafPredicts = booster.predictLeaf(testMat, 0);
        }
        catch (XGBoostError xgBoostError) {
            xgBoostError.printStackTrace();
        }
    }
}
