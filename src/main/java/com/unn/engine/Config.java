package com.unn.engine;

import com.unn.common.operations.Agent;

public class Config {
	public static String PRIMER = "primer";
	public static String ID = "id";
	public static int STIM_MIN = -1;
	public static int STIM_MAX =  1;
	public static int STIM_NULL = 0;
	public static int STIM_RANGE = STIM_MAX - STIM_MIN;
	public static boolean ASSERT = true;
	public static Agent MYSELF = new Agent()
		.withType("miner")
		.withProtocol("http")
		.withHost("localhost")
		.withPort(7000);
	public static int DEFAULT_NUMERIC_CLUSTER_COUNT = 10;
	public static int DEFAULT_DISCRETE_LABEL_COUNT = 10;
}
