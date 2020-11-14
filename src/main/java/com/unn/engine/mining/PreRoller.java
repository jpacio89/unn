package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.functions.Threshold;
import com.unn.engine.functions.ValueTime;
import com.unn.engine.Config;
import com.unn.engine.mining.models.Artifact;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.utils.MultiplesHashMap;
import com.unn.engine.utils.RandomManager;

public class PreRoller {
	HashMap<IFunctor, Integer> operatorIndex;
	ArrayList<IFunctor> leafs;
	ArrayList<Artifact.Portion> opHits;
	InnerDataset dataset;
	
	int reward;
	MultiplesHashMap<Integer, Artifact.Portion> findings;
	MultiplesHashMap<Artifact.Portion, Integer> opHitPresences;
	MiningStatusObservable miningStatusObservable;
	
	public PreRoller(InnerDataset dataset, int reward, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.opHits = new ArrayList<>();
		this.operatorIndex = new HashMap<>();
		this.reward = reward;
		this.miningStatusObservable = statusObservable;
	}
	
	public void init(ArrayList<IFunctor> leafs, ArrayList<IFunctor> booleanLayer) {
		this.leafs = leafs;
		this.opHits.clear();
		
		ArrayList<IFunctor> operators = booleanLayer;
		int i = 0;
		
		for (IFunctor operator : operators) {
			this.opHits.add(new Artifact.Portion(operator, Config.STIM_MIN));
			this.opHits.add(new Artifact.Portion(operator, Config.STIM_MAX));
			this.operatorIndex.put(operator, i * 2);
			i++;
		}

		this.findings = new MultiplesHashMap<>();
		this.opHitPresences = new MultiplesHashMap<>();
	}
	
	public static ArrayList<IFunctor> getBooleanParameters (ArrayList<IFunctor> args) {
		ArrayList<IFunctor> booleanParameters = new ArrayList<>();
		ArrayList<IFunctor> paramsWithContants = new ArrayList<>();
		paramsWithContants.addAll(args);
		
		for (int i = Config.STIM_MIN; i <= Config.STIM_MAX; ++i) {
			paramsWithContants.add(new Raw(i));
		}

		for (int i = 0; i < paramsWithContants.size(); ++i) {
			for (int j = 0; j < paramsWithContants.size(); ++j) {
				IFunctor lh = paramsWithContants.get(i);
				IFunctor rh = paramsWithContants.get(j);
				if (!lh.isParameter() && !rh.isParameter()) {
					continue;
				}
				Threshold thd = new Threshold(lh, rh);
				thd.setDescriptor(new FunctionDescriptor(thd.toString()));
				booleanParameters.add(thd);
			}
		}
		
		return booleanParameters;
	}
	
	public boolean checkTime(IFunctor op, int time, int hitToCheck) throws Exception {
		Integer val = this.dataset.getValueByTime(op, time);
		
		if (val == null) {
			calculate(op, time);
			val = this.dataset.getValueByTime(op, time);
		}

		assert val != null;
		return val == hitToCheck;
	}

	void setPresencesByOpHit(Artifact.Portion opHit, ArrayList<Integer> badTimes) throws Exception {
		for (Integer time : badTimes) {
			boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
			if (!isCheck) {
				findings.put(time, opHit);
				opHitPresences.put(opHit, time);
			}
		}
	}
	
	public Artifact createMatrix(ArrayList<Integer> goodTimes, ArrayList<Integer> badTimes) throws Exception {
		ArrayList<Integer> missingBadTimes = new ArrayList<>(badTimes);
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
				chosenSet.add(opHit);
			}
		}

		return new Artifact(chosenSet, this.reward);
	}
	
	private void calculate(IFunctor operator, Integer time) throws Exception {
		HashMap<IFunctor, Integer> values = new HashMap<>();
		
		for (IFunctor param : this.leafs) {
			values.put(param, dataset.getValueByTime(param, time));
		}
		
		int binaryResult = operator.operate(values);
		Integer oldValue = dataset.getValueByTime(operator, time);
		
		if (oldValue == null) {
			dataset.add(new ValueTime(operator, binaryResult, time));
		} else if (oldValue != binaryResult) {
			throw new Exception();
		}
	}
	
	
}
