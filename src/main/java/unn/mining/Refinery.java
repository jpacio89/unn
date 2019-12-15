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
	
	class MyRunnable extends Thread {
		int index;
		public double error;
		long[] tmpWeights;
		
		public MyRunnable(long[] _weights, int index) {
			this.index = index;
			this.tmpWeights = Arrays.copyOf(_weights, _weights.length);
		}
		
		@Override
		public void run() {
			this.tmpWeights[index]++;
			this.error = calculateError(tmpWeights);
		}
	};
	
	public Model refine() throws Exception {
		ArrayList<Artifact> artifacts = model.getArtifacts();
		long[] weights = new long[artifacts.size()];
		double[] errors = new double[weights.length];
		double lastError = 100000.0;
		
		for (int j = 0; j < 500; ++j) {			
			ArrayList<MyRunnable> threads = new ArrayList<MyRunnable>();
			
			for (int i = 0; i < weights.length; ++i) {
				MyRunnable thrd = new MyRunnable(weights, i);
				threads.add(thrd);
			}
			
			int concurrentThreads = 10;
			
			for (int i = 0; i < threads.size(); i += concurrentThreads) {
				for (int l = 0; l < concurrentThreads; ++l) {
					int idx = i + l;
					if (idx >= threads.size()) {
						continue;
					}
					threads.get(idx).start();
				}
				
				for (int l = 0; l < concurrentThreads; ++l) {
					int idx = i + l;
					if (idx >= threads.size()) {
						continue;
					}
					try {
						threads.get(idx).join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			for (int i = 0; i < weights.length; ++i) {
				errors[i] = threads.get(i).error;
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
			
			lastError = minError;
		}
		
		for (int i = 0; i < weights.length; ++i) {
			artifacts.get(i).weight = weights[i];
		}
		
		miner.gatherStats(model);
		
		return model;
		// return refinedModel;
	}
	
	class PreviousState {
		// Integer => time
		// Long => Boolean[] wasHit
		// Pair<Double, Integer>: Double => prediction, Integer => hitCount
		HashMap<Integer, Pair<Boolean[], Pair>> history;
		
		public PreviousState() {
			this.history = new HashMap<Integer, Pair<Boolean[], Pair>>();
		}
		
		public long getTotalHits(int time) {
			return (long) history.get(time).second().second();
		}
		
		public long getPrediction(int time) {
			return (long) history.get(time).second().first();
		}
	}
	
	public double calculateError (PreviousState prevState, long[] weights, int artifactIndex) {
		ArrayList<Integer> highs = this.miner.getHighs();
		ArrayList<Integer> lows = this.miner.getLows();
		
		double errorSum = 0.0;
		// TODO: if prevState => assign previous error sum
		
		if (prevState == null) {
			for (Integer high : highs) {
				Pair<Double, Boolean[]> data = this.model.predictPlusHits(high, weights);
				Double prediction = data.first();
				if (prediction == null) {
					errorSum += Config.STIMULI_RANGE;
				} else {
					errorSum += Config.STIMULI_MAX_VALUE - prediction;
				}
			}
		} else {
			for (Integer high : highs) {
				Artifact fact = this.model.getArtifacts().get(artifactIndex);
				boolean isHit = this.model.isHit(artifactIndex);
				int hitDiff = 0;
				
				if (isHit) {
					hitDiff += weights[artifactIndex];
				}
				
				if (wasHit) {
					hitDiff -= prevWeights[artifactIndex];
				}
				
				double predictionDiff = (prevState.getTotalHits(high) * prevState.getPrediction(high) + hitDiff * fact.reward) / (prevState.getTotalHits(high) * hitDiff);
				errorSum += predictionDiff;
			}
		}
		
		if (prevState == null) {
			for (Integer low : lows) {
				Pair<Double, Boolean[]> data = this.model.predictPlusHits(low, weights);
				Double prediction = data.first();
				if (prediction == null) {
					errorSum += Config.STIMULI_RANGE;
				} else {
					errorSum += prediction - Config.STIMULI_MIN_VALUE;
				}
			}
		} else {
			
		}
		
		return errorSum;
	}
}
