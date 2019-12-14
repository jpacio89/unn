package unn.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import unn.dataset.Dataset;
import unn.structures.Config;
import utils.RandomManager;

public class Refinery {
	private Model model;
	private Miner miner;
	
	public Refinery(Miner miner, Model model) {
		this.model = model;
		this.miner = miner;
	}
	
	public Model refine() {
		ArrayList<Artifact> artifacts = model.getArtifacts();
		long[] weights = new long[artifacts.size()];
		double[] errors = new double[weights.length];
		
		for (int j = 0; j < 10; ++j) {
			for (int i = 0; i < weights.length; ++i) {
				long[] tmpWeights = Arrays.copyOf(weights, weights.length);
				tmpWeights[i]++;
				errors[i] = calculateError(tmpWeights);
				
			//	System.out.println(String.format("Minimum Error: %f", minError));
			//	System.out.println(String.format("Errors: %s", Arrays.toString(errors)));
			//	System.out.println(String.format("Weights: %s", Arrays.toString(weights)));
			}
			
			double minError = Arrays.stream(errors).min().getAsDouble();
			
			System.out.println(String.format("Minimum Error: %f", minError));
			
			int minErrorIndex = -1;
			for (int k = 0; k < errors.length; ++k) {
				if (errors[k] == minError) {
					minErrorIndex = k;
					break;
				}
			}
			
			weights[minErrorIndex]++;
			
			if (minError == 0) {
				break;
			}
		}
		
		for (int i = 0; i < weights.length; ++i) {
			artifacts.get(i).weight = weights[i];
		}
		
		miner.gatherStats(model);
		
		return model;
		// return refinedModel;
	}
	
	public double calculateError (long[] weights) {
		ArrayList<Integer> highs = this.miner.getHighs();
		ArrayList<Integer> lows = this.miner.getLows();
		
		double errorSum = 0.0;
		
		for (Integer high : highs) {
			Double prediction = this.model.predictOne(high, weights);
			if (prediction == null) {
				errorSum += Config.STIMULI_RANGE;
			} else {
				errorSum += Config.STIMULI_MAX_VALUE - prediction;
			}
		}
		
		for (Integer low : lows) {
			Double prediction = this.model.predictOne(low, weights);
			if (prediction == null) {
				errorSum += Config.STIMULI_RANGE;
			} else {
				errorSum += prediction - Config.STIMULI_MIN_VALUE;
			}
		}
		
		return errorSum;
	}
}
