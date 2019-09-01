package utils;

public class SequentialToken 
{
	private static int MAX_TOKEN = 1024 * 1024;
	private static int last_token_ = 0;
	
	public static int next () {
		last_token_ = last_token_ + 1 % MAX_TOKEN;
		return last_token_++;
	}
}
