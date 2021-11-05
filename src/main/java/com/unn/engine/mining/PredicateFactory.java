package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFeature;
import com.unn.engine.Config;
import com.unn.engine.mining.models.Predicate;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.common.utils.MultiplesHashMap;
import com.unn.engine.utils.RandomManager;

public class PredicateFactory {
	ArrayList<IFeature> features;
	ArrayList<Predicate.Condition> allConditions;
	InnerDataset dataset;
	
	int reward;
	MultiplesHashMap<Predicate.Condition, Integer> opHitPresences;
	MiningStatusObservable miningStatusObservable;
	
	public PredicateFactory(InnerDataset dataset, int reward, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.allConditions = new ArrayList<>();
		this.reward = reward;
		this.miningStatusObservable = statusObservable;
	}
	
	public void init(ArrayList<IFeature> _features) {
		this.features = _features;
		this.allConditions.clear();

		for (IFeature feature : _features) {
			this.allConditions.add(new Predicate.Condition(feature, Config.get().STIM_MIN));
			this.allConditions.add(new Predicate.Condition(feature, Config.get().STIM_MAX));
		}

		this.opHitPresences = new MultiplesHashMap<>();
	}
	
	public boolean checkTime(IFeature op, int time, int hitToCheck) {
		return this.dataset.getValueByTime(op, time) == hitToCheck;
	}

	void setPresencesByOpHit(Predicate.Condition condition, ArrayList<Integer> badTimes) {
		for (Integer time : badTimes) {
			boolean isCheck = checkTime(condition.operator, time, condition.hit);
			if (!isCheck) {
				opHitPresences.put(condition, time);
			}
		}
	}

	ArrayList<Integer> getGoodRemovalsByOpHit(Predicate.Condition opHit, ArrayList<Integer> goodTimes) {
		return goodTimes.stream()
			.filter(time -> !checkTime(opHit.operator, time, opHit.hit))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	public Predicate randomPredicate(ArrayList<Integer> goodTimes, ArrayList<Integer> badTimes) throws Exception {
		ArrayList<Integer> missingBadTimes = new ArrayList<>(badTimes);
		ArrayList<Integer> remainingGoodTimes = new ArrayList<>(goodTimes);
		ArrayList<Predicate.Condition> availableOpHits = new ArrayList<>(this.allConditions);
		ArrayList<Predicate.Condition> chosenSet = new ArrayList<>();

		while (missingBadTimes.size() > 0) {
			Predicate.Condition opHit = RandomManager.getOne(availableOpHits);
			availableOpHits.remove(opHit);
			ArrayList<Integer> opHitTimes = opHitPresences.get(opHit);

			if (opHitTimes == null) {
				setPresencesByOpHit(opHit, badTimes);
				opHitTimes = opHitPresences.get(opHit);
			}

			if (opHitTimes != null) {
				if (!missingBadTimes.removeAll(opHitTimes)) {
					continue;
				}
				ArrayList<Integer> goodRemovals = getGoodRemovalsByOpHit(opHit, goodTimes);
				remainingGoodTimes.removeAll(goodRemovals);
				chosenSet.add(opHit);
			}
		}

		if (remainingGoodTimes.size() == 0) {
			return null;
		}

		return new Predicate(chosenSet, this.reward, remainingGoodTimes);
	}

	public ArrayList<IFeature> getFeatures() {
		return features;
	}
	
}
