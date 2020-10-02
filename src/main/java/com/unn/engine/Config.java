package com.unn.engine;

import com.unn.common.operations.Agent;

public class Config {
	public static String PRIMER = "primer";
	public static String ID = "id";
	public static int STIM_MIN = -20;
	public static int STIM_MAX =  20;
	public static int STIM_NULL = 0;
	public static int STIM_RANGE = STIM_MAX - STIM_MIN;
	public static boolean ASSERT = true;
	public static Agent MYSELF = new Agent()
		.withType("miner")
		.withProtocol("http")
		.withHost("localhost")
		.withPort(7000);
}
