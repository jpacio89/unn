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
