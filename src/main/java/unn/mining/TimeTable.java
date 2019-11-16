package unn.mining;

import java.util.ArrayList;
import java.util.HashMap;

import unn.dataset.Dataset;
import unn.interfaces.IOperator;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.operations.THD;
import unn.structures.Config;
import unn.structures.VTR;
import utils.MultiplesHashMap;
import utils.RandomManager;

public class TimeTable {
	HashMap<IOperator, Integer> operatorIndex;
	ArrayList<IOperator> leafs;
	ArrayList<OperatorHit> opHits;
	Dataset dataset;
	
	int reward;
	MultiplesHashMap<Integer, OperatorHit> findings;
	MultiplesHashMap<OperatorHit, Integer> opHitPresences;
	
	public TimeTable(Dataset dataset, int reward) {
		this.dataset = dataset;
		this.opHits = new ArrayList<OperatorHit>();
		this.operatorIndex = new HashMap<>();
		this.reward = reward;
	}
	
	public void init(ArrayList<IOperator> leafs, ArrayList<IOperator> booleanLayer) {
		this.leafs = leafs;
		this.opHits.clear();
		
		ArrayList<IOperator> operators = booleanLayer;
		int i = 0;
		
		for (IOperator operator : operators) {
			this.opHits.add(new OperatorHit(operator, Config.STIMULI_MIN_VALUE));
			this.opHits.add(new OperatorHit(operator, Config.STIMULI_MAX_VALUE));
			this.operatorIndex.put(operator, i * 2);
			i++;
		}
	}
	
	public static ArrayList<IOperator> getBooleanParameters (ArrayList<IOperator> args) {
		ArrayList<IOperator> booleanParameters = new ArrayList<IOperator>();
		
		ArrayList<IOperator> paramsWithContants = new ArrayList<IOperator>();
		paramsWithContants.addAll(args);
		
		for (int i = Config.STIMULI_MIN_VALUE; i <= Config.STIMULI_MAX_VALUE; ++i) {
			paramsWithContants.add((IOperator) new RAW(i));
		}
		
		int counter = 0;
		
		for (int i = 0; i < paramsWithContants.size(); ++i) {
			for (int j = 0; j < paramsWithContants.size(); ++j) {
				IOperator lh = paramsWithContants.get(i);
				IOperator rh = paramsWithContants.get(j);
				
				if (!lh.isParameter() && !rh.isParameter()) {
					continue;
				}
				
				THD thd = new THD(lh, rh);
				thd.setDescriptor(new OperatorDescriptor (".", thd.toString(), counter));
				
				booleanParameters.add(thd);
				counter++;
			}
		}
		
		return booleanParameters;
	}
	
	public boolean checkTime(IOperator op, int time, int hitToCheck) {
		Integer val = this.dataset.getValueByTime(op, time);
		
		if (val == null) {
			calculate(op, time);
			val = this.dataset.getValueByTime(op, time);
		}

		assert val != null;
		return val == hitToCheck;
	}
	
	public void presetFindings(ArrayList<Integer> badTimes) {
		this.findings = new MultiplesHashMap<>();
		this.opHitPresences = new MultiplesHashMap<>();
		
		long n = 0;
		long maxN = badTimes.size() * this.opHits.size();
		
		for (Integer time : badTimes) {
			for (OperatorHit opHit : this.opHits) {
				boolean isCheck = checkTime(opHit.operator, time, opHit.hit);
				if (!isCheck) {
					findings.put(time, opHit);
					opHitPresences.put(opHit, time);
				}
				if (n % 10000 == 0) {
					System.out.println((n * 100.0 / maxN) + "%");
				}
				n++;
			}
		}
	}
	
	public Artifact createMatrix(ArrayList<Integer> goodTimes, ArrayList<Integer> badTimes) {
		ArrayList<Integer> missingBadTimes = new ArrayList<Integer>(badTimes);		
		ArrayList<OperatorHit> availableOpHits = new ArrayList<OperatorHit>(this.opHits);
		ArrayList<OperatorHit> chosenSet = new ArrayList<OperatorHit>();
		
		while (missingBadTimes.size() > 0) {
			assert availableOpHits.size() > 0;
			
			OperatorHit opHit = RandomManager.getOne(availableOpHits);
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
			for (OperatorHit opHit : chosenSet) {
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
		
		return new Artifact(chosenSet, chosenSetWheatTimes, this.reward);
	}
	
	private void calculate(IOperator operator, Integer time) {
		operator.recycle();
		
		for (IOperator param : this.leafs) {
			param.define(dataset.getValueByTime(param, time));
		}
		
		try {
			operator.operate();
			int binaryResult = operator.value();
			dataset.add(new VTR(operator, binaryResult, time, this.dataset.getRewardByTime(time)));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
