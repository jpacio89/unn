package unn.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import unn.dataset.Dataset;
import unn.structures.Config;
import utils.Pair;
import utils.RandomManager;
import utils.Triplet;

public class Refinery {
	private Model model;
	private Miner miner;
	
	public Refinery(Miner miner, Model model) {
		this.model = model;
		this.miner = miner;
	}
	
	public Model refine() throws Exception {
		ArrayList<Artifact> artifacts = model.getArtifacts();
		Long[] weights = new Long[artifacts.size()];
		Arrays.fill(weights, 0L);
		double[] errors = new double[weights.length];
		double lastError = 100000.0;
		
		PreviousState prevState = null;
		
		for (int j = 0; j < 500; ++j) {
			PreviousState[] state = new PreviousState[weights.length];
			
			for (int i = 0; i < weights.length; ++i) {
				Long[] tmpWeights = Arrays.copyOf(weights, weights.length);
				tmpWeights[i]++;				
				state[i] = calculateError(prevState, tmpWeights, i);
				errors[i] = state[i].getErrorSum();
			}
			
			//	System.out.println(String.format("Minimum Error: %f", minError));
			//	System.out.println(String.format("Errors: %s", Arrays.toString(errors)));
			//	System.out.println(String.format("Weights: %s", Arrays.toString(weights)));
			
			double minError = Arrays.stream(errors).min().getAsDouble();
			
			System.out.println(String.format("Minimum Error: %f", minError));
			
			int minErrorIndex = -1;
			for (int k = 0; k < errors.length; ++k) {
				if (errors[k] == minError) {
					minErrorIndex = k;
					break;
				}
			}
		
			if (minError == lastError) {
				break;
			} else if (minError > lastError) {
				throw new Exception();
			}
			
			weights[minErrorIndex]++;
			
			if (minError < 1.0 || lastError - minError < 0.5) {
				break;
			}
			
			prevState = state[minErrorIndex];
			lastError = minError;
		}
		
		for (int i = 0; i < weights.length; ++i) {
			artifacts.get(i).weight = weights[i];
		}
		
		miner.gatherStats(model);
		
		return model;
		// return refinedModel;
	}
	
	public PreviousState calculateError (PreviousState prevState, Long[] weights, int artifactIndex) {
		ArrayList<Integer> highs = this.miner.getHighs();
		ArrayList<Integer> lows = this.miner.getLows();
		
		double errorSum = 0.0;
		
		if (prevState != null) {
			errorSum = prevState.getErrorSum();
		}
		
		PreviousState state = new PreviousState();
		state.setPreviousWeights(weights);
		
		errorSum += calculateErrorPartial (prevState, state, weights, artifactIndex, highs, true);
		errorSum += calculateErrorPartial (prevState, state, weights, artifactIndex, lows, false);
		
		state.setErrorSum(errorSum);
		
		return state;
	}
	
	private double calculateErrorPartial (
			PreviousState prevState,
			PreviousState state,
			Long[] weights, 
			int artifactIndex,
			ArrayList<Integer> times,
			boolean isHigh) {
		double errorSum = 0.0;
		
		if (prevState == null) {
			for (Integer time : times) {
				Pair<Boolean[], Pair> data = this.model.predictPlusHits(time, weights);
				state.history.put(time, data);
				Object obj = data.second().first();
				if (obj == null) {
					errorSum += Config.STIMULI_RANGE;
				} else {
					Double prediction = (Double) obj;
					if (isHigh) {
						errorSum += Config.STIMULI_MAX_VALUE - prediction;
					} else {
						errorSum += prediction - Config.STIMULI_MIN_VALUE;
					}
				}
				
			}
		} else {
			for (Integer time : times) {
				state.history.put(time, new Pair<Boolean[], Pair>(new Boolean[weights.length], new Pair(null, 0L)));
				state.copyHits(prevState, time);
				Artifact fact = this.model.getArtifacts().get(artifactIndex);
				Boolean isHit = this.model.isHit(time, artifactIndex);
				Boolean wasHit = prevState.wasHit(time, artifactIndex);
				long totalHits = prevState.getTotalHits(time);
				
				state.setHit(time, artifactIndex, isHit);
				
				if (isHit && wasHit) {
					long hitDiff = 0;
					hitDiff += weights[artifactIndex];
					Long[] previousWeights = prevState.getPreviousWeights(time);
					hitDiff -= previousWeights[artifactIndex];
					
					long totalHitsNew = totalHits + hitDiff;
					Double prediction = prevState.getPrediction(time);
					double predictionNew = (totalHits * prediction + hitDiff * fact.reward) / totalHitsNew;			
					state.setPrediction(time, artifactIndex, predictionNew, totalHitsNew);
					if (isHigh) {
						errorSum += (Config.STIMULI_MAX_VALUE - predictionNew) - (Config.STIMULI_MAX_VALUE - prediction);
					} else {
						errorSum += (predictionNew - Config.STIMULI_MIN_VALUE) - (prediction - Config.STIMULI_MIN_VALUE);
					}
				} else if (isHit || wasHit) {					
					if (isHit) {
						long totalHitsNew = weights[artifactIndex];
						double predictionNew = fact.reward;
						state.setPrediction(time, artifactIndex, predictionNew, totalHitsNew);
						if (isHigh) {
							errorSum += (Config.STIMULI_MAX_VALUE - predictionNew) - Config.STIMULI_RANGE;
						} else {
							errorSum += (predictionNew - Config.STIMULI_MIN_VALUE) - Config.STIMULI_RANGE;
						}
					} else {
						Double prediction = prevState.getPrediction(time);
						state.setPrediction(time, artifactIndex, null, 0L);
						if (isHigh) {
							errorSum += Config.STIMULI_RANGE - (Config.STIMULI_MAX_VALUE - prediction);
						} else {
							errorSum += Config.STIMULI_RANGE - (prediction - Config.STIMULI_MIN_VALUE);
						}
					}
				}
			}
		}
		
		return errorSum;
	}
}
