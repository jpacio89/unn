package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.functions.Threshold;
import com.unn.engine.functions.ValueTime;
import com.unn.engine.Config;
import com.unn.engine.mining.models.Artifact;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.common.utils.MultiplesHashMap;
import com.unn.engine.utils.RandomManager;

public class PreRoller {
	ArrayList<IFunctor> leafs;
	ArrayList<Artifact.Portion> opHits;
	InnerDataset dataset;
	
	int reward;
	MultiplesHashMap<Artifact.Portion, Integer> opHitPresences;
	MiningStatusObservable miningStatusObservable;
	
	public PreRoller(InnerDataset dataset, int reward, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.opHits = new ArrayList<>();
		this.reward = reward;
		this.miningStatusObservable = statusObservable;
	}
	
	public void init(ArrayList<IFunctor> leafs, ArrayList<IFunctor> booleanLayer) {
		this.leafs = leafs;
		this.opHits.clear();
		
		ArrayList<IFunctor> operators = booleanLayer;

		for (IFunctor operator : operators) {
			this.opHits.add(new Artifact.Portion(operator, Config.STIM_MIN));
			this.opHits.add(new Artifact.Portion(operator, Config.STIM_MAX));
		}

		this.opHitPresences = new MultiplesHashMap<>();
	}
	
	public boolean checkTime(IFunctor op, int time, int hitToCheck) {
		return this.dataset.getValueByTime(op, time) == hitToCheck;
	}

	void setPresencesByOpHit(Artifact.Portion opHit, ArrayList<Integer> badTimes) {
		for (Integer time : badTimes) {
			boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
			if (!isCheck) {
				opHitPresences.put(opHit, time);
			}
		}
	}

	ArrayList<Integer> getGoodRemovalsByOpHit(Artifact.Portion opHit, ArrayList<Integer> goodTimes) {
		return goodTimes.stream()
			.filter(time -> !checkTime(opHit.operator, time, opHit.hit))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	public Artifact createMatrix(ArrayList<Integer> goodTimes, ArrayList<Integer> badTimes) throws Exception {
		ArrayList<Integer> missingBadTimes = new ArrayList<>(badTimes);
		ArrayList<Integer> remainingGoodTimes = new ArrayList<>(goodTimes);
		ArrayList<Artifact.Portion> availableOpHits = new ArrayList<>(this.opHits);
		ArrayList<Artifact.Portion> chosenSet = new ArrayList<>();

		while (missingBadTimes.size() > 0) {
			Artifact.Portion opHit = RandomManager.getOne(availableOpHits);
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

		return new Artifact(chosenSet, this.reward, remainingGoodTimes);
	}
	
}
