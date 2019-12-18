package unn.mining;

import java.util.ArrayList;
import java.util.HashMap;

import utils.Pair;
import utils.Triplet;

public class PreviousState {
	HashMap<Integer, ArrayList<Long>> history;
	Long[] weights;
	Double errorSum;
	
	public PreviousState() {
		this.history = new HashMap<Integer, ArrayList<Long>>();
	}
	
	public void setPreviousWeights(Long[] previousWeights) {
		this.weights = previousWeights;
	}
	
	public Long[] getPreviousWeights(int time) {
		return weights;
	}
	
	public void setHitWeights(int time, ArrayList<Long> hitWeights) {
		this.history.put(time, hitWeights);
	}

	public ArrayList<Long> getHitWeights(Integer time) {
		return this.history.get(time);
	}

	public void setError(double errorSum) {
		this.errorSum = errorSum;
	}
	
	public double getError() {
		return this.errorSum;
	}
}