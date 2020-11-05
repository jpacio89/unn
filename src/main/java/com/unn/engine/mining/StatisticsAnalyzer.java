package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.engine.Config;

public class StatisticsAnalyzer implements Serializable {
	ArrayList<Integer> possibleValues;
	ConfusionMatrix matrix;
	
	public StatisticsAnalyzer() {
		this.possibleValues = new ArrayList<>();
		this.possibleValues.add(Config.STIM_MIN);
		this.possibleValues.add(Config.STIM_NULL);
		this.possibleValues.add(Config.STIM_MAX);
		this.matrix = new ConfusionMatrix(possibleValues.size());
	}
	
	public void addHit2Matrix(Integer expected, double guess) {
		int relaxation = (int) (0.25 * Config.STIM_MAX);

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
			this.matrix.inc(guessIndex, expectedIndex);
		} else {
			System.err.println("|StatsWalker| Unexpected situation");
		}
	}

	public ConfusionMatrix getConfusionMatrix() {
		return this.matrix;
	}
}
