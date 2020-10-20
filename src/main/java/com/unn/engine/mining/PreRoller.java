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
	
	public void presetFindings(ArrayList<Integer> badTimes) throws Exception {
		this.findings = new MultiplesHashMap<>();
		this.opHitPresences = new MultiplesHashMap<>();
		
		long n = 0;
		long maxN = badTimes.size() * this.opHits.size();
		
		for (Integer time : badTimes) {
			for (Artifact.Portion opHit : this.opHits) {
				boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
				if (!isCheck) {
					findings.put(time, opHit);
					opHitPresences.put(opHit, time);
				}
				if (n % 10000 == 0) {
					System.out.println((n * 100.0 / maxN) + "%");
				}
				this.miningStatusObservable.updateProgress(n, maxN);
				n++;
			}
		}
	}
	
	public Artifact createMatrix(ArrayList<Integer> goodTimes, ArrayList<Integer> badTimes) throws Exception {
		ArrayList<Integer> missingBadTimes = new ArrayList<Integer>(badTimes);		
		ArrayList<Artifact.Portion> availableOpHits = new ArrayList<Artifact.Portion>(this.opHits);
		ArrayList<Artifact.Portion> chosenSet = new ArrayList<Artifact.Portion>();
		
		while (missingBadTimes.size() > 0) {
			assert availableOpHits.size() > 0;
			
			Artifact.Portion opHit = RandomManager.getOne(availableOpHits);
			availableOpHits.remove(opHit);
			
			ArrayList<Integer> opHitTimes = opHitPresences.get(opHit);
			
			// TODO: check this
			if (opHitTimes == null || opHitTimes.size() == 0) {
				continue;
			}
			
			boolean  anyRemoved = false;
			
			for (Integer opHitTime : opHitTimes) {
				boolean wasRemoved = missingBadTimes.remove(opHitTime);
				if (wasRemoved) {
					anyRemoved = true;
				}
			}
			
			if (!anyRemoved) {
				continue;
			}
			
			chosenSet.add(opHit);
		}
		
		/* boolean weedFound = false;

		for (Integer time : badTimes) {
			boolean isRemoved = false;
			for (OperatorHit opHit : chosenSet) {
				boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
				if (!isCheck) {
					isRemoved = true;
					break;
				}
			}
			if (!isRemoved) {
				weedFound = true;
				break;
			}
		}
		
		assert weedFound == false; */

		ArrayList<Integer> chosenSetWheatTimes = new ArrayList<Integer>();
		
		for (Integer time : goodTimes) {
			boolean isRemoved = false;
			for (Artifact.Portion opHit : chosenSet) {
				boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
				if (!isCheck) {
					isRemoved = true;
					break;
				}
			}
			if (!isRemoved) {
				chosenSetWheatTimes.add(time);
			}
		}
		
		Artifact artifact = new Artifact(chosenSet, chosenSetWheatTimes, this.reward);
		
		/*if (Config.ASSERT) {
			boolean ret = checkNegatedArtifact(artifact, goodTimes, badTimes);
			
			if (!ret) {
				return null;
			}
		}*/
		
		return artifact;
	}
	
	private boolean checkNegatedArtifact(Artifact artifact, ArrayList<Integer> goodTimes, ArrayList<Integer> badTimes) throws Exception {
		long timeCounter = 0;
		for (Integer time : badTimes) {
			boolean isValid = false;
			for (Artifact.Portion opHit : artifact.opHits) {
				boolean ret = checkTime(opHit.operator, time, -opHit.hit);
				if (ret) {
					isValid = true;
					break;
				}
			}
			if (isValid) {
				timeCounter++;
			}
		}
		
		if (timeCounter == 0) {
			return false;
			// throw new Exception("|TimeTable| Negated artifact has hits");
		}
		
		return true;
	}
	
	private void calculate(IFunctor operator, Integer time) throws Exception {
		// operator.recycle();
		HashMap<IFunctor, Integer> values = new HashMap<IFunctor, Integer>();
		
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
