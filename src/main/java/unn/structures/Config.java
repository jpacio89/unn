package unn.structures;

public class Config {
	public static int STIMULI_MIN_VALUE = -20;
	public static int STIMULI_MAX_VALUE =  20;
	public static int STIMULI_NULL_VALUE = 0;
	public static int STIMULI_RANGE = STIMULI_MAX_VALUE - STIMULI_MIN_VALUE;
	
	public static int STIMULI_FALSE = STIMULI_MIN_VALUE;
	public static int STIMULI_TRUE = STIMULI_MAX_VALUE;
	
	public static boolean ASSERT = true;
	
	// Database options
	public static String DATABASE_NAME = "rabbitpt_unn_trade";
	public static String DATABASE_USERNAME = "rabbitpt_unn_trade_u";
	public static String DATABASE_PASSWORD = "Fne{q=CHF~lN";

	public static final String DATACENTER_PROTOCOL = "http";
	public static final String DATACENTER_HOST = "localhost";
	public static final int DATACENTER_PORT = 8002;
	
}
