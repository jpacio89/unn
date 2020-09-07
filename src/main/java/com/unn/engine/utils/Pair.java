package com.unn.engine.utils;

import java.io.Serializable;

public class Pair<L, R> implements Serializable {
	private static final long serialVersionUID = 4921130870402170339L;
	public L first_;
	public R second_;
	
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
