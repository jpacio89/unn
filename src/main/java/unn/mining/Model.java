package unn.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import unn.dataset.Dataset;
import unn.interfaces.IOperator;
import utils.Pair;
import utils.Triplet;

public class Model {
	final int TEST_SAMPLE_COUNT = 1000;
	
	StatsWalker walker;
	Dataset dataset;
	ArrayList<Artifact> artifacts;
	
	public Model(Dataset dataset) {
		this.dataset = dataset;
		this.artifacts = new ArrayList<Artifact>();
		this.walker = new StatsWalker();
	}
	
	public Model(Dataset dataset, ArrayList<Artifact> sublist) {
		this.dataset = dataset;
		this.artifacts = sublist;
	}
	
	public void add(Artifact artifact) {
		this.artifacts.add(artifact);
	}
	
	public StatsWalker getStatsWalker() {
		return this.walker;
	}
	
	public void gatherStats (ArrayList<Integer> testTimesLow, ArrayList<Integer> testTimesHigh) {
		this.walker = new StatsWalker();
		
		ArrayList<Integer> testTimes = new ArrayList<Integer>();
		
		testTimes.addAll(testTimesLow);
		testTimes.addAll(testTimesHigh);
		
		Collections.shuffle(testTimes);
		
		testTimes = new ArrayList<Integer> (testTimes.subList(0, Math.min(TEST_SAMPLE_COUNT, testTimes.size())));
		
		for (Integer time : testTimes) {
			predict(time, walker);
		}
	}
	
	private void predict (int time, StatsWalker walker) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		Double prediction = this.predict(inputs, null, null);
		
		IOperator[] allArguments = this.dataset.getAllLeaves();
		int historicAction = this.dataset.getValueByTime(allArguments[allArguments.length - 1], time);
		
		if (prediction != null) {
			walker.addHit2Matrix(time, historicAction, (int) Math.round(prediction.doubleValue()));
		}
		else {
			walker.incUnknown();
		}
	}
	
	public Double predict(int time, Long[] weights, Long[] _hitWeights) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		return this.predict(inputs, weights, _hitWeights);
	}
	
	public Double predict(HashMap<IOperator, Integer> inputs, Long[] weights, Long[] _hitWeights) {
		Long[] hitWeights = _hitWeights;

		if (hitWeights == null) {
			hitWeights = predictionHits(inputs, weights);
		}
		
		double rewardAccumulator = 0;
		long hitCount = 0;
			
		for (int i = 0; i < hitWeights.length; ++i) {
			Long weight = hitWeights[i];

			if (weight == 0) {
				continue;
			}
			
			Artifact artifact = this.artifacts.get(i);			
			rewardAccumulator += artifact.reward * weight * 1.0;
			hitCount += weight;
		}
		
		if (hitCount == 0) {
			return null;
		}
		
		rewardAccumulator /= hitCount;
		
		return rewardAccumulator;
	}
	
	public Long[] predictionHits (int time, Long[] weights) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		return predictionHits(inputs, weights);
	}
	
	public Long[] predictionHits(HashMap<IOperator, Integer> inputs, Long[] weights) {
		Long[] hitWeights = new Long[this.artifacts.size()];
		
		for (int i = 0; i < this.artifacts.size(); ++i) {
			Artifact artifact = this.artifacts.get(i);
			Long weight = artifact.weight;
			
			if (weights != null) {
				weight = weights[i];
			}

			if (weight != null && weight == 0) {
				hitWeights[i] = 0L;
				continue;
			}
			
			boolean hit = isHit(inputs, i);
			
			if (hit) {
				long w = 1;
				if (weight != null) {
					w = weight;
				}				
				hitWeights[i] = w;
			} else {
				hitWeights[i] = 0L;
			}
		}
		
		return hitWeights;
	}
	
	public Boolean isHit (int time, int artifactIndex) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		return isHit(inputs, artifactIndex);
	}
	
	public Long artifactHits(Integer time, int artifactIndex, Long[] weights) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		boolean isHit = isHit(inputs, artifactIndex);
		if (isHit) {
			return weights[artifactIndex];
		}
		return 0L;
	}
	
	public Boolean isHit(HashMap<IOperator, Integer> inputs, int artifactIndex) {
		Artifact artifact = this.artifacts.get(artifactIndex);
		ArrayList<OperatorHit> parcels = artifact.opHits;
		
		boolean hit = true;
		
		for (OperatorHit parcel : parcels) {
			IOperator thd = parcel.operator;
			Integer parcelOutcome = parcel.hit;
			
			try {
				int thdOutcome = thd.operate(inputs);
				
				if (parcelOutcome.intValue() != thdOutcome) {
					hit = false;
					break;
				}
			}
			catch (Exception e) {
				hit = false;
				e.printStackTrace();
			}
		}
		
		return hit;
	}

	public ArrayList<IOperator> getInputs() {
		return this.dataset.getTrainingLeaves();
	}
	
	public ArrayList<Artifact> getArtifacts() {
		return this.artifacts;
	}

	public Dataset getDataset() {
		return this.dataset;
	}
	
	private HashMap<IOperator, Integer> getInputsByTime(int time) {
		HashMap<IOperator, Integer> inputs = new HashMap<IOperator, Integer>();
		
		for (IOperator param : this.dataset.getTrainingLeaves()) {
			int val = this.dataset.getValueByTime(param, time);
			inputs.put(param, val);
		}
		
		return inputs;
	}
}
