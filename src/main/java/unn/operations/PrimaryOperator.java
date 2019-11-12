package unn.operations;

import unn.structures.Config;

public class PrimaryOperator {
	public static int Threshold (int v, int lb, int ub) {
		if(v >= lb && v <= ub) {
			return Config.STIMULI_MAX_VALUE;
		}
		
		return Config.STIMULI_MIN_VALUE;
	}
}
