package com.unn.engine.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RandomManager 
{
	private static Random randomizer_;
	
	// lb => inclusive, ub => inclusive
	public static int rand (int lb, int ub)
	{
		if (randomizer_ == null)
		{
			randomizer_ = new Random (System.currentTimeMillis ());
		}
		
		int dif = ub - lb + 1;
		return lb + randomizer_.nextInt (dif);
	}

	public static boolean getBoolean() {
		int guess = rand(0, 1);
		return guess == 0;
	}
	
	
	public static <T> T getOne (ArrayList<T> vals) {
		if (vals == null || vals.size() == 0) {
			return null;
		}
		int rndIndex = rand(0, vals.size() - 1);
		return vals.get (rndIndex);
	}
	
	public static <T> ArrayList<T> getMany (ArrayList<T> vals, int howMany) {
		Collections.shuffle(vals);
		
		int retCount = Math.min(howMany, vals.size());
		ArrayList<T> output = new ArrayList<T>();
		
		for (int i = 0; i < retCount; ++i) {
			output.add(vals.get(i));
		}
		
		return output;
	}
}
