package com.unn.engine;

import com.unn.common.operations.Agent;

public class Config {
	public static boolean ASSERT_MODE = true;
	public static String PRIMER = "primer";
	public static String ID = "id";
	public static int STIM_MIN = -10;
	public static int STIM_MAX =  10;
	public static int STIM_NULL = 0;
	public static int STIM_RANGE = STIM_MAX - STIM_MIN;
	public static Agent MYSELF = new Agent()
		.withType("miner")
		.withProtocol("http")
		.withHost("localhost")
		.withPort(7000);
	public static int DEFAULT_NUMERIC_CLUSTER_COUNT = 20;
	public static int DEFAULT_DISCRETE_LABEL_COUNT = 10;
	public static int MAX_UNKNOWN_RATE = 80;
	public static int MIN_ACCURACY_RATE = 60;
}
