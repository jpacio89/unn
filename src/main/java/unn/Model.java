package unn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class Model {
	final int TEST_SAMPLE_COUNT = 1000;
	
	StatsWalker walker;
	Dataset dataset;
	ArrayList<Artifact> artifacts;
	
	public Model(Dataset dataset) {
		this.dataset = dataset;
		this.artifacts = new ArrayList<Artifact>();
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
		double rewardAccumulator = 0;
		int hitCount = 0;
		
		for (Artifact artifact : this.artifacts) {
			ArrayList<OperatorHit> parcels = artifact.opHits;
			Integer percentage = artifact.reward;
			
			boolean hit = true;
			
			for (OperatorHit parcel : parcels) {
				IOperator thd = parcel.operator;
				Integer parcelOutcome = parcel.hit;
				
				thd.recycle();
				
				for (IOperator param : this.dataset.getTrainingLeaves()) {
					param.define(this.dataset.getValueByTime(param, time));
				}
				
				try {
					thd.operate();
					int thdOutcome = thd.value();
					
					if (parcelOutcome.intValue() != thdOutcome) {
						hit = false;
						break;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}	
			}
			
			if (hit) {
				rewardAccumulator += percentage;
				hitCount++;
			}
		}
		
		rewardAccumulator /= hitCount;
		
		IOperator[] allArguments = this.dataset.getAllLeaves();
		int historicAction = this.dataset.getValueByTime(allArguments[allArguments.length - 1], time);
		
		walker.addHit2Matrix(time, historicAction, (int) rewardAccumulator);
	}

	public Double predict(HashMap<IOperator, Integer> inputs) {
		double rewardAccumulator = 0;
		int hitCount = 0;
		
		for (Artifact artifact : this.artifacts) {
			ArrayList<OperatorHit> parcels = artifact.opHits;
			Integer percentage = artifact.reward;
			
			boolean hit = true;
			
			for (OperatorHit parcel : parcels) {
				IOperator thd = parcel.operator;
				Integer parcelOutcome = parcel.hit;
			
				thd.recycle();
				
				for (IOperator param : this.dataset.getTrainingLeaves()) {
					if (inputs.containsKey(param)) {
						param.define(inputs.get(param));
					} else {
						System.out.println("Missing assignment: " + param.toString());
					}
				}
				
				try {
					thd.operate();
					int thdOutcome = thd.value();
					
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
				rewardAccumulator += percentage;
				hitCount++;
			}
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
}
