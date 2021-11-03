package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.engine.Config;

public class PerformanceAnalyzer implements Serializable {
	ArrayList<Integer> possibleValues;
	ConfusionMatrix matrix;
	
	public PerformanceAnalyzer() {
		this.possibleValues = new ArrayList<>();
		this.possibleValues.add(Config.STIM_MIN);
		this.possibleValues.add(Config.STIM_NULL);
		this.possibleValues.add(Config.STIM_MAX);
		this.matrix = new ConfusionMatrix(possibleValues.size());
	}
	
	public void addEvent(Integer expected, double guess) throws Exception {
		// TODO: put 0.8 in Config
		int relaxation = (int) (0.8 * Config.STIM_MAX);

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

		if (expectedIndex < 0 || expectedIndex == 1) {
			throw new Exception(String.format("PerformanceAnalyzer: expected value either MIN or MAX. Instead '%d' was found.", expected));
		} else if (guessIndex < 0) {
			throw new Exception(String.format("PerformanceAnalyzer: guess value is weird: '%d'", guess));
		}

		this.matrix.inc(guessIndex, expectedIndex);
	}

	public ConfusionMatrix getConfusionMatrix() {
		return this.matrix;
	}
}
