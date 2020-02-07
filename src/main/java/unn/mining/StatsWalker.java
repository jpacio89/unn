package unn.mining;

import java.io.Serializable;
import java.util.ArrayList;
import unn.structures.Config;

public class StatsWalker implements Serializable {
	private static final long serialVersionUID = -5115126641533695064L;
	ArrayList<Integer> possibleValues;
	int[][] hitMatrix;
	int outlier;
	
	ArrayList<Integer> times;
	
	public StatsWalker() {
		this.possibleValues = new ArrayList<Integer>();
		this.possibleValues.add(Config.STIMULI_MIN_VALUE);
		this.possibleValues.add(Config.STIMULI_NULL_VALUE);
		this.possibleValues.add(Config.STIMULI_MAX_VALUE);
		
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
	
	public void addHit2Matrix(Integer time, Integer expected, Integer guess) {
		int relaxation = (int) 0.1 * Config.STIMULI_MAX_VALUE * 2;
		
		if (guess > 0) {
			guess = guess > Config.STIMULI_MAX_VALUE - relaxation ? Config.STIMULI_MAX_VALUE : guess;
		} else {
			guess = guess < Config.STIMULI_MIN_VALUE + relaxation ? Config.STIMULI_MIN_VALUE : guess;
		}
		
		int guessIndex = this.possibleValues.indexOf(guess);
		int expectedIndex = this.possibleValues.indexOf(expected);
		
		if (guessIndex < 0) {
			guessIndex = this.possibleValues.indexOf(Config.STIMULI_NULL_VALUE);
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
}
