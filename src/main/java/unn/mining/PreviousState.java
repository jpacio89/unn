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
	
	public Double getPrediction(int time) {
		Object pred = history.get(time).second().first();
		return pred == null ? null : (double) pred;
	}
	
	public void setPreviousWeights(Long[] previousWeights) {
		this.weights = previousWeights;
	}
	
	public Long[] getPreviousWeights(int time) {
		return weights;
	}
	
	public boolean wasHit(int time, int index) {
		Boolean wasHit = history.get(time).first()[index];
		return wasHit != null && wasHit == true;
	}
	
	public void copyHits(PreviousState previous, int time) {
		Boolean[] hits = this.history.get(time).first();
		Boolean[] prevHits = previous.history.get(time).first();
		for (int i = 0; i < hits.length; ++i) {
			hits[i] = prevHits[i];
		}
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

	public void setPrediction(Integer time, int artifactIndex, Double predictionNew, long totalHitsNew) {
		this.history.get(time).second(new Pair<Double, Long>(predictionNew, totalHitsNew));
		
	}
}