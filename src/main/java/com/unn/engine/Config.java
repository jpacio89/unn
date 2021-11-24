package com.unn.engine;

import com.unn.common.operations.Agent;

public class Config {
	static Config instance;

	public boolean ASSERT_MODE = true;
	public String PRIMER = "primer";
	public String ID = "id";
	public int STIM_MIN = -10;
	public int STIM_MAX =  10;
	public int STIM_NULL = 0;
	public int STIM_RANGE = STIM_MAX - STIM_MIN;
	public double MODEL_PREDICTION_ROUNDING_FACTOR = 0.8;
	public int MODEL_PREDICTION_PREDICATE_HIT_COUNT = 30;
	public int MINING_TIME = 1 * 60 * 1000;
	public Agent MYSELF = new Agent()
		.withType("miner")
		.withProtocol("http")
		.withHost("localhost")
		.withPort(7000);
	public int DEFAULT_NUMERIC_CLUSTER_COUNT = 50;
	public int DEFAULT_DISCRETE_LABEL_COUNT = 100;
	public int MAX_UNKNOWN_RATE = 80;
	public int MIN_ACCURACY_RATE = 60;

	public Config() { }

	public static Config get () {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	public static void set (Config _config) {
		instance = _config;
	}
}
