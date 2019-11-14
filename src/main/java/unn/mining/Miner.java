package unn.mining;

import java.util.ArrayList;
import java.util.Collections;

import unn.dataset.Dataset;
import unn.interfaces.IOperator;
import unn.structures.Config;

public class Miner {
	public final int MIN_WHEAT_COUNT = 1;
	public final long MINING_TIME = 1 * 60 * 1000;
	
	Dataset dataset;
	Model model;
	boolean isReady;
	
	ArrayList<ArrayList<Integer>> trainTimeSets;
	ArrayList<ArrayList<Integer>> testTimeSets;
	ArrayList<TimeTable> timetables;

	private long miningStartTime;
	
	public Miner(Dataset dataset) {
		this.dataset = dataset;
		this.trainTimeSets = new ArrayList<ArrayList<Integer>>();
		this.testTimeSets = new ArrayList<ArrayList<Integer>>();
		this.timetables = new ArrayList<TimeTable>();
		this.isReady = false;
	}
	
	public void init() throws Exception {
		this.trainTimeSets.clear();
		this.testTimeSets.clear();
		this.timetables.clear();
		
		this.model = new Model(this.dataset);
		int ratio = 1;
		
		ArrayList<Integer> allTimesLow  = dataset.getTimesByReward(Config.STIMULI_MIN_VALUE, dataset.count(Config.STIMULI_MIN_VALUE) / ratio);
		ArrayList<Integer> allTimesNull  = dataset.getTimesByReward(Config.STIMULI_NULL_VALUE, dataset.count(Config.STIMULI_NULL_VALUE) / ratio);
		if (allTimesNull != null) {
			allTimesLow.addAll(allTimesNull);
		}
		
		ArrayList<Integer> allTimesHigh = dataset.getTimesByReward(Config.STIMULI_MAX_VALUE, dataset.count(Config.STIMULI_MAX_VALUE));
		
		if (allTimesLow == null || allTimesHigh == null) {
			return;
		}
		
		Collections.shuffle(allTimesLow);
		Collections.shuffle(allTimesHigh);
		
		int midPointLow = allTimesLow.size() / 2;
		int midPointHigh = allTimesHigh.size() / 2;
		
		System.out.println(String.format(" Training set size: [ %d lows, %d highs ]", midPointLow, midPointHigh));
		
		this.trainTimeSets.add(new ArrayList<Integer> (allTimesLow.subList(0, midPointLow)));
		this.trainTimeSets.add(new ArrayList<Integer> (allTimesHigh.subList(0, midPointHigh)));

		this.testTimeSets.add(new ArrayList<Integer> (allTimesLow.subList(midPointLow, allTimesLow.size())));
		this.testTimeSets.add(new ArrayList<Integer> (allTimesHigh.subList(midPointHigh, allTimesHigh.size())));
		
		assertDisjoint();
		
		ArrayList<IOperator> booleanLayer = TimeTable.getBooleanParameters (dataset.getTrainingLeaves());
		Integer[] rewards = { Config.STIMULI_MAX_VALUE, Config.STIMULI_MIN_VALUE };
		int i = 0;
		
		for (Integer reward : rewards) {
			TimeTable table = new TimeTable(dataset, reward);
			
			System.out.println(String.format(" TimeTable init: %d", reward));
			
			table.init(dataset.getTrainingLeaves(), booleanLayer);
			
			System.out.println(String.format(" TimeTable preset: %d", reward));
			
			table.presetFindings(this.trainTimeSets.get(i));
			this.timetables.add(table);
			i++;
		}
		
		this.isReady = true;
	}
	
	public Model getModel() {
		return this.model;
	}
	
	public ArrayList<Integer> getHighs() {
		return this.trainTimeSets.get(1);
	}
	
	public ArrayList<Integer> getLows() {
		return this.trainTimeSets.get(0);
	}
	
	public void mine() {
		startClock();
		
		for (int i = 0; alive(); i = (i + 1) % this.timetables.size()) {
			Artifact newArtifact = null;
			
			TimeTable table = this.timetables.get(i);
			
			assert i < this.timetables.size();
			
			if (i == 0) {
				newArtifact = table.createMatrix(this.trainTimeSets.get(1), this.trainTimeSets.get(0));
			} else {
				newArtifact = table.createMatrix(this.trainTimeSets.get(0), this.trainTimeSets.get(1));
			}
			
			if (newArtifact.getWheatCount() > MIN_WHEAT_COUNT /* && !Artifact.isRepetition(artifacts, newArtifact) */) {
				model.add(newArtifact);
				model.gatherStats(this.testTimeSets.get(0), this.testTimeSets.get(1));
			}
			
			if ((System.currentTimeMillis() - this.miningStartTime) % 10000 < 1000) {
				System.out.println(String.format("Mining... %d",  System.currentTimeMillis() - this.miningStartTime));
			}
		}
	}
	
	public void gatherStats(Model model) {
		model.gatherStats(this.testTimeSets.get(0), this.testTimeSets.get(1));
	}
	
	private void startClock() {
		this.miningStartTime = System.currentTimeMillis();
	}
	
	private boolean alive() {
		return System.currentTimeMillis() - this.miningStartTime < MINING_TIME;
	}

	private void assertDisjoint() throws Exception {
		assert this.trainTimeSets.size() == this.testTimeSets.size();
		
		for (int i = 0; i < this.trainTimeSets.size(); ++i) {
			ArrayList<Integer> timesetTrain = this.trainTimeSets.get(i);
			ArrayList<Integer> timesetTest = this.testTimeSets.get(i);
			
			boolean isDisjoint = Collections.disjoint(timesetTrain, timesetTest);
			
			if (!isDisjoint) {
				throw new Exception("Train and Test datasets are not disjoint.");
			}
			
			assert isDisjoint;
		}
	}

	public boolean ready() {
		return this.isReady;
	}
}
