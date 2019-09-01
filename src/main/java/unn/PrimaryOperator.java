package unn;

public class PrimaryOperator {
	public static int Threshold (int v, int lb, int ub) {
		if(v >= lb && v <= ub) {
			return Config.STIMULI_MAX_VALUE;
		}
		
		return Config.STIMULI_MIN_VALUE;
	}
	
	public static int Threshold (int v, int lb) {
		return Threshold (v, lb, Config.STIMULI_MAX_VALUE - 1); 
	}
	
	public static int Calculate (IOperator root, Integer x, Integer y) {
		return Threshold (x, y);
	}
	
	public static int K (Integer in, Integer k) {
		if (in == Config.STIMULI_MAX_VALUE) {
			return k;
		}
		return Config.STIMULI_MIN_VALUE;
	}
	
}
