package unn.mining;

import java.util.HashMap;

import utils.Pair;
import utils.Triplet;

public class PreviousState {
	HashMap<Integer, Long[]> history;
	Long[] weights;
	Double errorSum;
	
	public PreviousState() {
		this.history = new HashMap<Integer, Long[]>();
	}
	
	public void setPreviousWeights(Long[] previousWeights) {
		this.weights = previousWeights;
	}
	
	public Long[] getPreviousWeights(int time) {
		return weights;
	}
	
	public void setHitWeights(int time, Long[] hitWeights) {
		this.history.put(time, hitWeights);
	}

	public Long[] getHitWeights(Integer time) {
		return this.history.get(time);
	}

	public void setError(double errorSum) {
		this.errorSum = errorSum;
	}
	
	public double getError() {
		return this.errorSum;
	}
}