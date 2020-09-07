package com.unn.engine.utils;

public class Triplet<L, M, R> {
	private L first_;
	private M second_;
	private R third_;
	
	public Triplet (L a, M b, R c)
	{
		first_ = a;
		second_ = b;
		third_ = c;
	}

	public L first () {
		return first_;
	}

	public M second () {
		return second_;
	}

	public R third () {
		return third_;
	}
	
	public void first (L first) {
		first_ = first;
	}
	
	public void second (M second) {
		second_ = second;
	}	

	public void third (R third) {
		third_ = third;
	}	
}

