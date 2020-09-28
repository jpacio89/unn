package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.functions.Threshold;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.Config;
import com.unn.engine.utils.MultiplesHashMap;
import com.unn.engine.utils.RandomManager;

public class PreRoller {
	HashMap<IOperator, Integer> operatorIndex;
	ArrayList<IOperator> leafs;
	ArrayList<ArtifactParcel> opHits;
	InnerDataset dataset;
	
	int reward;
	MultiplesHashMap<Integer, ArtifactParcel> findings;
	MultiplesHashMap<ArtifactParcel, Integer> opHitPresences;
	MiningStatusObservable miningStatusObservable;
	
	public PreRoller(InnerDataset dataset, int reward, MiningStatusObservable statusObservable) {
		this.dataset = dataset;
		this.opHits = new ArrayList<ArtifactParcel>();
		this.operatorIndex = new HashMap<>();
		this.reward = reward;
		this.miningStatusObservable = statusObservable;
	}
	
	public void init(ArrayList<IOperator> leafs, ArrayList<IOperator> booleanLayer) {
		this.leafs = leafs;
		this.opHits.clear();
		
		ArrayList<IOperator> operators = booleanLayer;
		int i = 0;
		
		for (IOperator operator : operators) {
			this.opHits.add(new ArtifactParcel(operator, Config.STIMULI_MIN_VALUE));
			this.opHits.add(new ArtifactParcel(operator, Config.STIMULI_MAX_VALUE));
			this.operatorIndex.put(operator, i * 2);
			i++;
		}
	}
	
	public static ArrayList<IOperator> getBooleanParameters (ArrayList<IOperator> args) {
		ArrayList<IOperator> booleanParameters = new ArrayList<IOperator>();
		
		ArrayList<IOperator> paramsWithContants = new ArrayList<IOperator>();
		paramsWithContants.addAll(args);
		
		for (int i = Config.STIMULI_MIN_VALUE; i <= Config.STIMULI_MAX_VALUE; ++i) {
			paramsWithContants.add((IOperator) new Raw(i));
		}
		
		int counter = 0;
		
		for (int i = 0; i < paramsWithContants.size(); ++i) {
			for (int j = 0; j < paramsWithContants.size(); ++j) {
				IOperator lh = paramsWithContants.get(i);
				IOperator rh = paramsWithContants.get(j);
				
				if (!lh.isParameter() && !rh.isParameter()) {
					continue;
				}
				
				Threshold thd = new Threshold(lh, rh);
				thd.setDescriptor(new FunctionDescriptor(".", thd.toString(), counter));
				
				booleanParameters.add(thd);
				counter++;
			}
		}
		
		return booleanParameters;
	}
	
	public boolean checkTime(IOperator op, int time, int hitToCheck) throws Exception {
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
			for (ArtifactParcel opHit : this.opHits) {
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
		ArrayList<ArtifactParcel> availableOpHits = new ArrayList<ArtifactParcel>(this.opHits);
		ArrayList<ArtifactParcel> chosenSet = new ArrayList<ArtifactParcel>();
		
		while (missingBadTimes.size() > 0) {
			assert availableOpHits.size() > 0;
			
			ArtifactParcel opHit = RandomManager.getOne(availableOpHits);
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
			for (ArtifactParcel opHit : chosenSet) {
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
			for (ArtifactParcel opHit : artifact.opHits) {
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
	
	private void calculate(IOperator operator, Integer time) throws Exception {
		// operator.recycle();
		HashMap<IOperator, Integer> values = new HashMap<IOperator, Integer>();
		
		for (IOperator param : this.leafs) {
			values.put(param, dataset.getValueByTime(param, time));
		}
		
		int binaryResult = operator.operate(values);
		Integer oldValue = dataset.getValueByTime(operator, time);
		
		if (oldValue == null) {
			dataset.add(new ValueTimeReward(operator, binaryResult, time, this.dataset.getRewardByTime(time)));
		} else if (oldValue != binaryResult) {
			throw new Exception();
		}
	}
	
	
}
