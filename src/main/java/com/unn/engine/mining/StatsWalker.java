package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;
import com.unn.engine.Config;

public class StatsWalker implements Serializable {
	private static final long serialVersionUID = -5115126641533695064L;
	ArrayList<Integer> possibleValues;
	int[][] hitMatrix;
	int outlier;
	
	ArrayList<Integer> times;
	
	public StatsWalker() {
		this.possibleValues = new ArrayList<Integer>();
		this.possibleValues.add(Config.STIM_MIN);
		this.possibleValues.add(Config.STIM_NULL);
		this.possibleValues.add(Config.STIM_MAX);
		
		this.hitMatrix = new int[possibleValues.size()][possibleValues.size()];
		this.outlier = 0;
		
		this.times = new ArrayList<Integer>();
	}
	
	public int getUnknownCount() {
		return this.outlier;
	}
	
	public int[][] getHitMatrix() {
		return this.hitMatrix;
	}
	
	public void addHit2Matrix(Integer time, Integer expected, double guess) {
		int relaxation = (int) (0.1 * Config.STIM_MAX);

		if (guess > 0) {
			guess = guess >= Config.STIM_MAX - relaxation ? Config.STIM_MAX : guess;
		} else {
			guess = guess <= Config.STIM_MIN + relaxation ? Config.STIM_MIN : guess;
		}
		
		int guessIndex = this.possibleValues.indexOf((int) Math.round(guess));
		int expectedIndex = this.possibleValues.indexOf(expected);
		
		if (guessIndex < 0) {
			guessIndex = this.possibleValues.indexOf(Config.STIM_NULL);
		}
		
		if (guessIndex >= 0 && expectedIndex >= 0) {
			this.hitMatrix[guessIndex][expectedIndex]++;
		} else {
			System.err.println("|StatsWalker| Unexpected situation");
		}
		
		if (guessIndex == 2 && expectedIndex == 2) {
			this.times.add(time);
		}
	}
	
	public void incUnknown() {
		this.outlier++;
	}
	
	public void printTimes() {
		for(Integer time : times) {
			System.out.println(time);
		}
	}
	
	public void print() {		
		for (int i = 0; i < this.possibleValues.size(); ++i) {
			for (int j = 0; j < this.possibleValues.size(); ++j) {
				System.out.printf("%6d", this.hitMatrix[i][j]);
			}
			System.out.println();
		}
		System.out.println(String.format("Outliers = %d", this.outlier));
		System.out.println();
		System.out.println();
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public ArrayList<Integer> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(ArrayList<Integer> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public void setHitMatrix(int[][] hitMatrix) {
		this.hitMatrix = hitMatrix;
	}

	public int getOutlier() {
		return outlier;
	}

	public void setOutlier(int outlier) {
		this.outlier = outlier;
	}

	public ArrayList<Integer> getTimes() {
		return times;
	}

	public void setTimes(ArrayList<Integer> times) {
		this.times = times;
	}
}
