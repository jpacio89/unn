package utils;

public class Pair<L, R> {
	private L first_;
	private R second_;
	
	public Pair (L a, R b)
	{
		first_ = a;
		second_ = b;
	}

	public L first () {
		return first_;
	}

	public R second () {
		return second_;
	}
	
	public void first (L first) {
		first_ = first;
	}
	
	public void second (R second) {
		second_ = second;
	}

	@Override
	public String toString () 
	{
		return "Pair [" + first_ + ", " + second_ + "]";
	}
}
