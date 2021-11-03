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
	ArrayList<IFeature> leafs;
	ArrayList<Predicate.Condition> opHits;
	InnerDataset dataset;
	
	int reward;
	MultiplesHashMap<Predicate.Condition, Integer> opHitPresences;
	MiningStatusObservable miningStatusObservable;
	
	public PredicateFactory(InnerDataset dataset, int reward, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.opHits = new ArrayList<>();
		this.reward = reward;
		this.miningStatusObservable = statusObservable;
	}
	
	public void init(ArrayList<IFeature> leafs, ArrayList<IFeature> booleanLayer) {
		this.leafs = leafs;
		this.opHits.clear();
		
		ArrayList<IFeature> operators = booleanLayer;

		for (IFeature operator : operators) {
			this.opHits.add(new Predicate.Condition(operator, Config.STIM_MIN));
			this.opHits.add(new Predicate.Condition(operator, Config.STIM_MAX));
		}

		this.opHitPresences = new MultiplesHashMap<>();
	}
	
	public boolean checkTime(IFeature op, int time, int hitToCheck) {
		return this.dataset.getValueByTime(op, time) == hitToCheck;
	}

	void setPresencesByOpHit(Predicate.Condition opHit, ArrayList<Integer> badTimes) {
		for (Integer time : badTimes) {
			boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
			if (!isCheck) {
				opHitPresences.put(opHit, time);
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
		ArrayList<Predicate.Condition> availableOpHits = new ArrayList<>(this.opHits);
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

				// TODO: put 80.0% in Config
				//if (opHitTimes.size() * 100.0 / (goodRemovals.size() + opHitTimes.size()) < 5.0 ) {
				//	continue;
				//}

				remainingGoodTimes.removeAll(goodRemovals);
				chosenSet.add(opHit);
			}
		}

		return new Predicate(chosenSet, this.reward, remainingGoodTimes);
	}
	
}
