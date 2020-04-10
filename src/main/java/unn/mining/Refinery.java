package unn.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import unn.dataset.InnerDataset;
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
		double minError = 1000000000.0;
		ArrayList<Integer> subsetFinal = null;
		
		for(int i = 0; i < 10000; ++i) {
			double lastError = minError;
			
			ArrayList<Integer> subsetCandidate = new ArrayList<Integer>();
			
			for(int j = 0; j < 10; ++j) {
				while(true) {
					Artifact rndArtifact = RandomManager.getOne(artifacts);
					int index = artifacts.indexOf(rndArtifact);
					
					if (subsetCandidate.contains(index)) {
						continue;
					}
					
					Long[] weights = new Long[artifacts.size()];
					Arrays.fill(weights, 0L);
					weights[index] = 1L;
					
					for (int _index : subsetCandidate) {
						weights[_index] = 1L;
					}
					
					PreviousState state = calculateError(lastError, weights);
					
					if (state != null) {
						double error = state.getError();
						if (error < lastError) {
							lastError = error;
							subsetCandidate.add(index);
						} else {
							break;
						}
					} else {
						break;
					}
				}	
			}
			
			if (lastError < minError) {
				subsetFinal = subsetCandidate;
				minError = lastError;
				System.out.println(String.format("Minimum Error: %f", minError));
			}
			
			if (minError == 0.0) {
				break;
			}
		}
		
		ArrayList<Artifact> newArtifacts = new ArrayList<Artifact>();
		
		for(int index : subsetFinal) {
			newArtifacts.add(artifacts.get(index));
		}
		
		Model refined = new Model(this.model.getDataset(), newArtifacts);
		miner.gatherStats(refined);
		
		return refined;
	}
	
	public PreviousState calculateError (double maxError, Long[] weights) {
		ArrayList<Integer> highs = this.miner.getHighs();
		ArrayList<Integer> lows = this.miner.getLows();
		
		double errorSum = 0.0;
		
		PreviousState state = new PreviousState();
		Double err = null;
		
		
		err = calculateErrorPartial(maxError, weights, highs, true);
		
		if (err != null) {
			errorSum += err;
		} else {
			return null;
		}
		
		err = calculateErrorPartial(maxError, weights, lows, false);
		
		if (err != null) {
			errorSum += err;
		} else {
			return null;
		}
		
		state.setError(errorSum);
		
		return state;
	}
	
	private Double calculateErrorPartial (
			double maxError,
			Long[] weights,
			ArrayList<Integer> times,
			boolean isHigh) {
		
		double errorSum = 0.0;
		
		for (Integer time : times) {
			Pair<ArrayList<Long>, Pair<Double, Long>> hitWeights = this.model.predictionHits(time, weights);
			Pair<Double, Long> summary = hitWeights.second();
			
			Double prediction = null;
			
			if (summary.second() > 0) {
				prediction = summary.first() / summary.second();
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
			
			if (errorSum >= maxError) {
				return null;
			}
		}
		
		return errorSum;
	}

	
}
