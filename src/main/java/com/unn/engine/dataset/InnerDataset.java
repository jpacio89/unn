package com.unn.engine.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.functions.ValueTimeReward;
import com.unn.engine.utils.MultiplesHashMap;
import com.unn.engine.utils.RandomManager;

public class InnerDataset implements Serializable {
	private static final long serialVersionUID = 4804115730789995484L;
	ArrayList<Integer> times;
	HashMap<Integer, Integer> timedRewards;
	HashMap<IFunctor, HashMap<Integer, Integer>> timedValues;
	MultiplesHashMap<Integer, Integer> rewardedTimes;
	
	ArrayList<IFunctor> args;
	IFunctor[] localArgs;
	
	public InnerDataset() {
		this.times = new ArrayList<>();
		this.timedRewards = new HashMap<>();
		this.timedValues = new HashMap<>();
		this.rewardedTimes = new MultiplesHashMap<>();
	}
	
	public void shrink() {
		this.times.clear();
		this.rewardedTimes.clear();
		this.timedRewards.clear();
		this.timedValues.clear();
		this.rewardedTimes.clear();
	}
	
	public void add(ValueTimeReward vtr) {
		assert vtr.getClass() != null &&
				vtr.getValue() != null && 
				vtr.getTime() != null && 
				vtr.getReward() != null;
		
		if (!this.timedRewards.containsKey (vtr.getTime())) {
			this.times.add(vtr.getTime());
			this.rewardedTimes.put(vtr.getReward(), vtr.getTime());
		}
		
		if (this.timedRewards.containsKey (vtr.getTime())) {
			assert this.timedRewards.get (vtr.getTime()).intValue() == vtr.getReward().intValue();
		}
		
		this.timedRewards.put (vtr.getTime(), vtr.getReward());
		
		if (!this.timedValues.containsKey (vtr.getVTRClass())) {
			HashMap<Integer, Integer> classValues = new HashMap<>();
			classValues.put (vtr.getTime(), vtr.getValue());
			this.timedValues.put (vtr.getVTRClass(), classValues);
		} else {
			this.timedValues.get (vtr.getVTRClass()).put(vtr.getTime(), vtr.getValue());
		}
	}

	public ArrayList<Integer> getTimes() {
		return this.times;
	}
	
	public ArrayList<Integer> getTimesByReward(Integer reward, Integer elementCount) {
		ArrayList<Integer> times = this.rewardedTimes.get(reward);
		if (times == null) {
			return null;
		}
		return RandomManager.getMany(times, elementCount);
	}

	public Integer getValueByTime(IFunctor op, int time) {
		if (!this.timedValues.containsKey(op)) {
			return null;
		}
		return this.timedValues.get(op).get(time);
	}

	public Integer getRewardByTime(Integer time) {
		assert this.timedRewards.containsKey(time);
		return this.timedRewards.get(time);
	}
	
	public ArrayList<IFunctor> getTrainingLeaves() {
		return this.args;
	}
	
	public IFunctor[] getAllLeaves() {
		return this.localArgs;
	}
	
	public void setTrainingLeaves(ArrayList<IFunctor> leaves) {
		this.args = leaves;
	}
	
	public void setAllLeaves(ArrayList<IFunctor> leaves) {
		this.localArgs = leaves.toArray(new IFunctor[leaves.size()]);
	}

	public int count(int reward) {
		ArrayList<Integer> times = this.rewardedTimes.get(reward);
		if (times == null) {
			return 0;
		}
		return times.size();
		
	}
	
	public IFunctor getFunctorByClassName(String className) {
		for(IFunctor op : this.localArgs) {
			if (op.getDescriptor().getVtrName().equals(className)) {
				return op;
			}
		}
		return null;
	}

	public HashMap<IFunctor, Integer> bundleSample(int time) {
		HashMap<IFunctor, Integer> input = new HashMap<>();
		for (IFunctor functor : getAllLeaves()) {
			Integer value = getValueByTime(functor, time);
			input.put(functor, value);
		}
		return input;
	}
}
