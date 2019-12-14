package unn.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import unn.dataset.Dataset;
import unn.interfaces.IOperator;

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

	public Double predictOne(int time, long[] weights) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		Double prediction = this.predictOne(inputs, weights);
		return prediction;
	}
	
	private void predict (int time, StatsWalker walker) {
		HashMap<IOperator, Integer> inputs = this.getInputsByTime(time);
		Double prediction = this.predict(inputs);
		
		IOperator[] allArguments = this.dataset.getAllLeaves();
		int historicAction = this.dataset.getValueByTime(allArguments[allArguments.length - 1], time);
		
		if (prediction != null) {
			walker.addHit2Matrix(time, historicAction, (int) prediction.doubleValue());
		}
		else {
			walker.incUnknown();
		}
	}

	public Double predict(HashMap<IOperator, Integer> inputs) {
		double rewardAccumulator = 0;
		int hitCount = 0;
		
		/*for (IOperator param : this.dataset.getTrainingLeaves()) {
			if (inputs.containsKey(param)) {
				param.define(inputs.get(param));
			} else {
				System.out.println("Missing assignment: " + param.toString());
			}
		}*/	

		
		for (Artifact artifact : this.artifacts) {
			if (artifact.weight != null && artifact.weight == 0) {
				continue;
			}
			ArrayList<OperatorHit> parcels = artifact.opHits;
			Integer percentage = artifact.reward;
			
			boolean hit = true;
						
			for (OperatorHit parcel : parcels) {
				IOperator thd = parcel.operator;
				Integer parcelOutcome = parcel.hit;
			
				// thd.recycle();
				
				try {
					int thdOutcome = thd.operate(inputs);
					// int thdOutcome = thd.value();
					
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
			
			if (hit) {
				long w = 1;
				if (artifact.weight != null) {
					w = artifact.weight;
				}
				rewardAccumulator += percentage * w;
				hitCount += w;;
			}
		}
		
		if (hitCount == 0) {
			return null;
		}
		
		rewardAccumulator /= hitCount;
		
		return rewardAccumulator;
	}
	
	public Double predictOne(HashMap<IOperator, Integer> inputs, long[] weights) {
		double rewardAccumulator = 0;
		int hitCount = 0;
		int n = 0;
		
		/*for (IOperator param : this.dataset.getTrainingLeaves()) {
			if (inputs.containsKey(param)) {
				param.define(inputs.get(param));
			} else {
				System.out.println("Missing assignment: " + param.toString());
			}
		}*/
		
		for (Artifact artifact : this.artifacts) {
			if (weights[n] == 0) {
				n++;
				continue;
			}
			
			ArrayList<OperatorHit> parcels = artifact.opHits;
			Integer percentage = artifact.reward;
			
			boolean hit = true;
			
			for (OperatorHit parcel : parcels) {
				IOperator thd = parcel.operator;
				Integer parcelOutcome = parcel.hit;
			
				// thd.recycle();
				
				try {
					int thdOutcome = thd.operate(inputs);
					// int thdOutcome = thd.value();
					
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
			
			if (hit) {
				rewardAccumulator += percentage * weights[n];
				hitCount += weights[n];
			}
			
			n++;
		}
		
		if (hitCount == 0) {
			return null;
		}
		
		rewardAccumulator /= hitCount;
		
		return rewardAccumulator;
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
