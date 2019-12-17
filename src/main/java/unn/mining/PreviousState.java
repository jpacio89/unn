package unn.mining;

import java.util.HashMap;

import utils.Pair;
import utils.Triplet;

public class PreviousState {
	// Integer => time
	// Long => Boolean[] wasHit
	// Pair<Double, Integer>: Double => prediction, Integer => hitCount
	HashMap<Integer, Pair<Boolean[], Pair>> history;
	double errorSum;
	Long[] weights;
	
	public PreviousState() {
		this.history = new HashMap<Integer, Pair<Boolean[], Pair>>();
	}
	
	public long getTotalHits(int time) {
		return (long) history.get(time).second().second();
	}
	
	public long getPrediction(int time) {
		return (long) history.get(time).second().first();
	}
	
	public void setPreviousWeights(Long[] previousWeights) {
		this.weights = previousWeights;
	}
	
	public Long[] getPreviousWeights(int time) {
		return weights;
	}
	
	public boolean wasHit(int time, int index) {
		return history.get(time).first()[index];
	}
	
	public void setErrorSum(double _errorSum) {
		this.errorSum = _errorSum;
	}
	
	public double getErrorSum() {
		return this.errorSum;
	}

	public void setHit(Integer time, int artifactIndex, boolean isHit) {
		this.history.get(time).first()[artifactIndex] = isHit;
	}

	public void setPrediction(Integer time, int artifactIndex, double predictionNew, long totalHitsNew) {
		this.history.get(time).second(new Pair<Double, Long>(predictionNew, totalHitsNew));
		
	}
}