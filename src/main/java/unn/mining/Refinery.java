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
		double lastError = 1000000000.0;
		
		PreviousState prevState = null;
		
		for (int j = 0; j < 500; ++j) {
			PreviousState[] state = new PreviousState[weights.length];
			Double minError = 1000000000.0;
			PreviousState bestState = null;
			int minErrorIndex = -1;
			for (int i = 0; i < weights.length; ++i) {
				Long[] tmpWeights = Arrays.copyOf(weights, weights.length);
				tmpWeights[i]++;				
				state[i] = calculateError(prevState, tmpWeights, i);
				errors[i] = state[i].getError();
				if (errors[i] < minError) {
					minError = errors[i];
					bestState = state[i];
					minErrorIndex = i;
				}
				state[i] = null;
			}
			
			//	System.out.println(String.format("Minimum Error: %f", minError));
			//	System.out.println(String.format("Errors: %s", Arrays.toString(errors)));
			//	System.out.println(String.format("Weights: %s", Arrays.toString(weights)));
			
			//double minError = Arrays.stream(errors).min().getAsDouble();
			
			System.out.println(String.format("Minimum Error: %f", minError));
			
			/*int minErrorIndex = -1;
			for (int k = 0; k < errors.length; ++k) {
				if (errors[k] == minError) {
					minErrorIndex = k;
					break;
				}
			}*/
		
			if (minError == lastError) {
				break;
			} else if (minError > lastError) {
				throw new Exception();
			}
			
			weights[minErrorIndex]++;
			
			if (minError < 1.0 || lastError - minError < 0.5) {
				break;
			}
			
			prevState = bestState;
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
		
		PreviousState state = new PreviousState();
		
		errorSum += calculateErrorPartial (prevState, state, weights, artifactIndex, highs, true);
		errorSum += calculateErrorPartial (prevState, state, weights, artifactIndex, lows, false);
		
		state.setError(errorSum);
		
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
				Pair<ArrayList<Long>, Pair<Double, Long>> hitWeights = this.model.predictionHits(time, weights);
				state.setHitWeights(time, hitWeights);
			}
		}
		
		for (Integer time : times) {
			if (prevState != null) {
				Pair<ArrayList<Long>, Pair<Double, Long>> hitWeights = prevState.getHitWeights(time);
				Long artifactHits = this.model.artifactHits(time, artifactIndex, weights);
				ArrayList<Long> newHitWeights = new ArrayList<Long>();
				newHitWeights.addAll(hitWeights.first());
				newHitWeights.set(artifactIndex, artifactHits);
				
				long hitDiff = artifactHits - hitWeights.first().get(artifactIndex);
				Double accumulator = hitWeights.second().first();
				Long hitCount = hitWeights.second().second();
				Double newPrediction = 0.0;
				
				if (hitCount + hitDiff > 0) {
					newPrediction = (accumulator + (double) (hitDiff * this.model.getArtifacts().get(artifactIndex).reward));
				}
				
				Pair<Double, Long> newOutcome = new Pair<Double, Long>(newPrediction, hitCount + hitDiff);
				Pair<ArrayList<Long>, Pair<Double, Long>> newHitWeightsPair = new Pair<ArrayList<Long>, Pair<Double, Long>>(newHitWeights, newOutcome);
				state.setHitWeights(time, newHitWeightsPair);
			}
			
			Pair<ArrayList<Long>, Pair<Double, Long>> hitWeights = state.getHitWeights(time);
			Double prediction = null;
			
			if (hitWeights.second().second() > 0) {
				prediction = hitWeights.second().first() / hitWeights.second().second();
			}
			
			//Double predictionCheck = this.model.predict(time, weights, hitWeights.first());
			//System.out.println(String.format("%f vs %f", prediction, predictionCheck));
			
			if (prediction == null) {
				errorSum += Config.STIMULI_RANGE;
			} else if (isHigh) {
				errorSum += Config.STIMULI_MAX_VALUE - prediction;
			} else {
				errorSum += prediction - Config.STIMULI_MIN_VALUE;
			}	
		}
		
		return errorSum;
	}
}
