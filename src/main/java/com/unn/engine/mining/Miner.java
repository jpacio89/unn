package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.Config;
import com.unn.engine.mining.models.Predicate;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.mining.models.Model;

public class Miner {
	public final long MINING_TIME = 1 * 60 * 1000;
	
	InnerDataset dataset;
	Model model;
	boolean isReady;
	ArrayList<IFeature> featureBlacklist;
	IFeature miningTarget;

	MiningStatusObservable statusObservable;
	ArrayList<ArrayList<Integer>> trainTimeSets;
	ArrayList<PredicateFactory> predicateFactories;

	private long miningStartTime;

	ArrayList<Integer> trainTimes;
	
	public Miner(InnerDataset dataset, IFeature miningTarget, ArrayList<IFeature> featureBlacklist, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.trainTimeSets = new ArrayList<>();
		this.predicateFactories = new ArrayList<>();
		this.isReady = false;
		this.statusObservable = statusObservable;
		this.featureBlacklist = featureBlacklist;
		this.miningTarget = miningTarget;
	}
	
	public void init(ArrayList<Integer> trainTimes) throws Exception {
		this.trainTimes = trainTimes;
		this.trainTimeSets.clear();
		this.predicateFactories.clear();
		this.model = new Model(this.dataset, this.miningTarget);

		ArrayList<Integer> trainTimesLow  = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MIN, this.trainTimes);
		ArrayList<Integer> trainTimesHigh = dataset.getTimesByFunctor(
			this.miningTarget, Config.STIM_MAX, this.trainTimes);
		
		if (trainTimesLow.size() == 0 || trainTimesHigh.size() == 0) {
			return;
		}
		
		System.out.println(String.format("|Miner| Training set size: lows=%d, highs=%d",
			trainTimesLow.size(), trainTimesHigh.size()));

		this.trainTimeSets.add(trainTimesLow.stream()
			.collect(Collectors.toCollection(ArrayList::new)));
		this.trainTimeSets.add(trainTimesHigh.stream()
			.collect(Collectors.toCollection(ArrayList::new)));

		//if (Config.ASSERT_MODE) {
		//	assertDisjoint();
		//}

		// Note: this is to avoid feeding the solution to the miner
		ArrayList<IFeature> trainingFeatures = dataset.getFunctors().stream()
			.filter((functor) -> !featureBlacklist.contains(functor))
			.collect(Collectors.toCollection(ArrayList::new));

		if (trainingFeatures.size() == 0) {
			return;
		}


		Integer[] rewards = { Config.STIM_MAX, Config.STIM_MIN };

		for (Integer reward : rewards) {
			PredicateFactory factory = new PredicateFactory(dataset, reward, this.statusObservable);
			factory.init(trainingFeatures);
			this.predicateFactories.add(factory);
		}
		
		this.isReady = true;
	}
	
	public Model getModel() {
		return this.model;
	}
	
	public void mine() throws Exception {
		startClock();
		this.statusObservable.updateStatusLabel("MINING");
		
		for (int now = 0, next = 1; alive(); now = (now + 1) % 2, next = (next + 1) % 2) {
			PredicateFactory factory = this.predicateFactories.get(now);
			Predicate newPredicate = factory.randomPredicate(this.trainTimeSets.get(next),
				this.trainTimeSets.get(now));

			if (newPredicate != null &&
				Predicate.isRepetition(model.getPredicates(), newPredicate) == null) {
				model.add(newPredicate);
				System.out.println(String.format("|Miner| artifact count: %d\r", model.getPredicates().size()));
				this.statusObservable.updateArtifactCount(model.getPredicates().size());
			}
			
			this.statusObservable.updateProgress(System.currentTimeMillis() - this.miningStartTime, MINING_TIME);
		}

		model.sort();
	}
	
	private void startClock() {
		this.miningStartTime = System.currentTimeMillis();
	}
	
	private boolean alive() {
		return System.currentTimeMillis() - this.miningStartTime < MINING_TIME;
	}

	/*private void assertDisjoint() throws Exception {
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
	}*/

	public boolean ready() {
		return this.isReady;
	}
}
