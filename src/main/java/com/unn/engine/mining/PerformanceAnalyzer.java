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
		this.possibleValues.add(Config.get().STIM_MIN);
		this.possibleValues.add(Config.get().STIM_NULL);
		this.possibleValues.add(Config.get().STIM_MAX);
		this.matrix = new ConfusionMatrix(possibleValues.size());
	}
	
	public void addEvent(Integer expected, double guess) throws Exception {
		int relaxation = (int) (Config.get().MODEL_PREDICTION_ROUNDING_FACTOR * Config.get().STIM_MAX);

		if (guess > 0) {
			guess = guess >= Config.get().STIM_MAX - relaxation ? Config.get().STIM_MAX : guess;
		} else {
			guess = guess <= Config.get().STIM_MIN + relaxation ? Config.get().STIM_MIN : guess;
		}
		
		int guessIndex = this.possibleValues.indexOf((int) Math.round(guess));
		int expectedIndex = this.possibleValues.indexOf(expected);
		
		if (guessIndex < 0) {
			guessIndex = this.possibleValues.indexOf(Config.get().STIM_NULL);
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
