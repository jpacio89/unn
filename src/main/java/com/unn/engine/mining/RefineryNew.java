package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.Arrays;

import com.unn.engine.Config;
import com.unn.engine.mining.models.Artifact;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.models.Model;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.RandomManager;

public class RefineryNew {
	private Model model;
	private Miner miner;
	
	public RefineryNew(Miner miner, Model model) {
		this.model = model;
		this.miner = miner;
	}
	
	public Model refine() throws Exception {
		ArrayList<Artifact> artifacts = model.getArtifacts();
		double minError = 1000000000.0;
		ArrayList<Integer> subsetFinal = null;
		
		for(int i = 0; i < 1000; ++i) {
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
					
					JobConfig.PreviousState state = calculateError(lastError, weights);
					
					if (state != null) {
						double error = state.getError();
						if (error < lastError) {
							lastError = error;
							subsetCandidate.add(index);
							j = 0;
						} else {
							break;
						}
					} else {
						break;
					}
				}
				if(lastError == 0.0) {
					break;
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
		
		Model refined = new Model(this.model.getDataset(), newArtifacts, this.model.getRewardSelector());
		miner.gatherStats(refined);
		
		return refined;
	}
	
	public JobConfig.PreviousState calculateError (double maxError, Long[] weights) {
		ArrayList<Integer> highs = this.miner.getHighs();
		ArrayList<Integer> lows = this.miner.getLows();
		
		double errorSum = 0.0;
		
		JobConfig.PreviousState state = new JobConfig.PreviousState();
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
				errorSum += Config.STIM_RANGE;
			} else if (isHigh) {
				errorSum += Config.STIM_MAX - prediction;
			} else {
				errorSum += prediction - Config.STIM_MIN;
			}
			
			if (errorSum >= maxError) {
				return null;
			}
		}
		
		return errorSum;
	}

	
}
