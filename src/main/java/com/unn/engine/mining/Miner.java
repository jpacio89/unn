package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.Config;
import com.unn.engine.mining.models.Artifact;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.mining.models.Model;

public class Miner {
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

	ArrayList<Integer> trainTimes;
	ArrayList<Integer> testTimes;
	
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
	
	public void init(ArrayList<Integer> trainTimes, ArrayList<Integer> testTimes) throws Exception {
		this.trainTimes = trainTimes;
		this.testTimes = testTimes;

		this.trainTimeSets.clear();
		this.testTimeSets.clear();
		this.timetables.clear();
		this.model = new Model(this.dataset, this.miningTarget);

		ArrayList<Integer> trainTimesLow  = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MIN, this.trainTimes);
		ArrayList<Integer> trainTimesHigh = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MAX, this.trainTimes);
		ArrayList<Integer> testTimesLow  = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MIN, this.testTimes);
		ArrayList<Integer> testTimesHigh = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MAX, this.testTimes);
		
		if (trainTimesLow.size() == 0 || trainTimesHigh.size() == 0 ||
			testTimesLow.size() == 0 || testTimesHigh.size() == 0) {
			return;
		}
		
		System.out.println(String.format("|Miner| Training set size: lows=%d, highs=%d",
			trainTimesLow.size(), trainTimesHigh.size()));

		this.trainTimeSets.add(trainTimesLow.stream()
			.collect(Collectors.toCollection(ArrayList::new)));
		this.trainTimeSets.add(trainTimesHigh.stream()
			.collect(Collectors.toCollection(ArrayList::new)));
		this.testTimeSets.add(testTimesLow.stream()
			.collect(Collectors.toCollection(ArrayList::new)));
		this.testTimeSets.add(testTimesHigh.stream()
			.collect(Collectors.toCollection(ArrayList::new)));

		if (Config.ASSERT_MODE) {
			assertDisjoint();
		}

		ArrayList<IFunctor> trainingFunctors = dataset.getFunctors().stream()
			.filter((functor) -> !functorBlacklist.contains(functor))
			.collect(Collectors.toCollection(ArrayList::new));

		if (trainingFunctors.size() == 0) {
			return;
		}

		ArrayList<IFunctor> thresholdLayer = trainingFunctors; // PreRoller.getBooleanParameters(trainingFunctors);
		Integer[] rewards = { Config.STIM_MAX, Config.STIM_MIN };
		int i = 0;
		for (Integer reward : rewards) {
			PreRoller table = new PreRoller(dataset, reward, this.statusObservable);
			table.init(trainingFunctors, thresholdLayer);
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
		
		for (int now = 0, next = 1; alive(); now = (now + 1) % 2, next = (next + 1) % 2) {
			PreRoller table = this.timetables.get(now);
			Artifact newArtifact = table.createMatrix(this.trainTimeSets.get(next),
				this.trainTimeSets.get(now));

			if (newArtifact != null &&
				Artifact.isRepetition(model.getArtifacts(), newArtifact) == null) {
				model.add(newArtifact);
				System.out.println(String.format("|Miner| artifact count: %d\r", model.getArtifacts().size()));
				this.statusObservable.updateArtifactCount(model.getArtifacts().size());
			}
			
			this.statusObservable.updateProgress(System.currentTimeMillis() - this.miningStartTime, MINING_TIME);
		}

		model.sort();
		model.gatherStats(this.testTimeSets.get(0), this.testTimeSets.get(1));
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
		if (this.trainTimeSets.size() != this.testTimeSets.size()) {
			throw new Exception("Test and train datasets not set up properly");
		}

		ArrayList<ArrayList<Integer>> all = new ArrayList<>();
		all.addAll(this.trainTimeSets);
		all.addAll(this.testTimeSets);

		for (int i = 0; i < all.size(); ++i) {
			ArrayList<Integer> alpha = all.get(i);
			for (int j = i + 1; j < all.size(); ++j) {
				ArrayList<Integer> beta = all.get(j);
				boolean isDisjoint = Collections.disjoint(alpha, beta);
				if (!isDisjoint) {
					throw new Exception("Mining datasets are not disjoint");
				}
			}
		}
	}

	public boolean ready() {
		return this.isReady;
	}
}
