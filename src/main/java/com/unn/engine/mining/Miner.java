package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.Config;

public class Miner {
	public final int MIN_WHEAT_COUNT = 0;
	public final long MINING_TIME = 1 * 60 * 1000;
	
	InnerDataset dataset;
	Model model;
	boolean isReady;
	ArrayList<IFunctor> functorBlacklist;
	IFunctor miningTarget;

	MiningStatusObservable statusObservable;
	ArrayList<ArrayList<Integer>> trainTimeSets;
	ArrayList<ArrayList<Integer>> testTimeSets;
	ArrayList<PreRoller> timetables;

	private long miningStartTime;
	
	public Miner(InnerDataset dataset, IFunctor miningTarget, ArrayList<IFunctor> functorBlacklist, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.trainTimeSets = new ArrayList<>();
		this.testTimeSets = new ArrayList<>();
		this.timetables = new ArrayList<>();
		this.isReady = false;
		this.statusObservable = statusObservable;
		this.functorBlacklist = functorBlacklist;
		this.miningTarget = miningTarget;
	}
	
	public void init() throws Exception {
		this.trainTimeSets.clear();
		this.testTimeSets.clear();
		this.timetables.clear();
		this.model = new Model(this.dataset, this.miningTarget);

		ArrayList<Integer> allTimesLow  = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MIN);
		ArrayList<Integer> allTimesNull  = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_NULL);

		if (allTimesNull.size() > 0) {
			System.out.println("|Miner| ERROR -> found NULL times");
		}
		
		ArrayList<Integer> allTimesHigh = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MAX);
		
		if (allTimesLow.size() == 0 || allTimesHigh.size() == 0) {
			return;
		}
		
		Collections.shuffle(allTimesLow);
		Collections.shuffle(allTimesHigh);
		
		int midPointLow = allTimesLow.size() / 2;
		int midPointHigh = allTimesHigh.size() / 2;
		
		System.out.println(String.format("|Miner| Training set size: lows=%d, highs=%d",
			midPointLow, midPointHigh));

		this.trainTimeSets.add(allTimesLow.stream()
			.limit(midPointLow)
			.collect(Collectors.toCollection(ArrayList::new)));
		this.trainTimeSets.add(allTimesLow.stream()
			.limit(midPointHigh)
			.collect(Collectors.toCollection(ArrayList::new)));
		this.testTimeSets.add(allTimesLow.stream()
				.skip(midPointLow)
				.collect(Collectors.toCollection(ArrayList::new)));
		this.testTimeSets.add(allTimesLow.stream()
				.skip(midPointHigh)
				.collect(Collectors.toCollection(ArrayList::new)));

		if (Config.ASSERT) {
			assertDisjoint();
		}

		ArrayList<IFunctor> trainingFunctors = dataset.getFunctors().stream()
			.filter((functor) -> !functorBlacklist.contains(functor))
			.collect(Collectors.toCollection(ArrayList::new));
		ArrayList<IFunctor> thresholdLayer = PreRoller.getBooleanParameters(trainingFunctors);
		Integer[] rewards = { Config.STIM_MAX, Config.STIM_MIN };
		int i = 0;
		for (Integer reward : rewards) {
			PreRoller table = new PreRoller(dataset, reward, this.statusObservable);
			table.init(trainingFunctors, thresholdLayer);
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
	
	public void mine() throws Exception {
		startClock();
		this.statusObservable.updateStatusLabel("MINING");
		
		for (int i = 0; alive(); i = (i + 1) % this.timetables.size()) {
			Artifact newArtifact = null;
			
			PreRoller table = this.timetables.get(i);
			
			assert i < this.timetables.size();
			
			if (i == 0) {
				newArtifact = table.createMatrix(this.trainTimeSets.get(1), this.trainTimeSets.get(0));
			} else {
				newArtifact = table.createMatrix(this.trainTimeSets.get(0), this.trainTimeSets.get(1));
			}
			
			if (newArtifact != null && 
				newArtifact.getWheatCount() > MIN_WHEAT_COUNT && 
				Artifact.isRepetition(model.getArtifacts(), newArtifact) == null) {
				model.add(newArtifact);
				model.gatherStats(this.testTimeSets.get(0), this.testTimeSets.get(1));
				this.statusObservable.updateArtifactCount(model.getArtifacts().size());
			}
			
			//if ((System.currentTimeMillis() - this.miningStartTime) % 10000 < 1000) {
			//	System.out.println(String.format("Mining... %d",  System.currentTimeMillis() - this.miningStartTime));
			//}
			
			this.statusObservable.updateProgress(System.currentTimeMillis() - this.miningStartTime, MINING_TIME);
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
