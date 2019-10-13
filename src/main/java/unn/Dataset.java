package unn;

import java.util.ArrayList;
import java.util.HashMap;

import utils.MultiplesHashMap;
import utils.RandomManager;

public class Dataset {
	ArrayList<Integer> times;
	HashMap<Integer, Integer> timedRewards;
	HashMap<IOperator, HashMap<Integer, Integer>> timedValues;
	MultiplesHashMap<Integer, Integer> rewardedTimes;
	
	ArrayList<IOperator> args;
	IOperator[] localArgs;
	
	public Dataset() {
		this.times = new ArrayList<Integer>();
		this.timedRewards = new HashMap<Integer, Integer>();
		this.timedValues = new HashMap<IOperator, HashMap<Integer, Integer>>();
		this.rewardedTimes = new MultiplesHashMap<Integer, Integer>();
	}
	
	public void shrink() {
		this.times.clear();
		this.rewardedTimes.clear();
		this.timedRewards.clear();
		this.timedValues.clear();
		this.rewardedTimes.clear();
	}
	
	public void add(VTR vtr) {
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
			HashMap<Integer, Integer> classValues = new HashMap<Integer, Integer>();
			classValues.put (vtr.getTime(), vtr.getValue());
			this.timedValues.put (vtr.getVTRClass(), classValues);
		} else {
			this.timedValues.get (vtr.getVTRClass()).put(vtr.getTime(), vtr.getValue());
		}
	}
	
	public Integer getOneTime() {
		return RandomManager.getOne(this.times);
	}
	
	public boolean hasTime(int time) {
		return this.timedRewards.containsKey(time);
	}
	
	public Integer getTimeByReward(Integer reward) {
		ArrayList<Integer> times = this.rewardedTimes.get(reward);
		if (times == null) {
			return null;
		}
		return RandomManager.getOne(times);
	}
	
	public ArrayList<Integer> getTimesByReward(Integer reward, Integer elementCount) {
		ArrayList<Integer> times = this.rewardedTimes.get(reward);
		if (times == null) {
			return null;
		}
		return RandomManager.getMany(times, elementCount);
	}

	public Integer getValueByTime(IOperator op, int time) {		
		if (!this.timedValues.containsKey(op)) {
			return null;
		}
		return this.timedValues.get(op).get(time);
	}

	public Integer getRewardByTime(Integer time) {
		assert this.timedRewards.containsKey(time);
		return this.timedRewards.get(time);
	}
	
	public ArrayList<IOperator> getTrainingLeaves() {
		return this.args;
	}
	
	public IOperator[] getAllLeaves() {
		return this.localArgs;
	}
	
	public void setTrainingLeaves(ArrayList<IOperator> leaves) {
		this.args = leaves;
	}
	
	public void setAllLeaves(ArrayList<IOperator> leaves) {
		this.localArgs = leaves.toArray(new IOperator[leaves.size()]);
	}

	public int count(int reward) {
		ArrayList<Integer> times = this.rewardedTimes.get(reward);
		if (times == null) {
			return 0;
		}
		return times.size();
		
	}
	
	public IOperator getOperatorByClassName(String className) {
		for(IOperator op : this.localArgs) {
			if (op.getDescriptor().getVtrName().equals(className)) {
				return op;
			}
		}
		return null;
	}
}
