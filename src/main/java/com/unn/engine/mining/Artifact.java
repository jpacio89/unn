package com.unn.engine.mining;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.interfaces.IOperator;
import com.unn.engine.utils.CombinationUtils;

public class Artifact implements Serializable {
	private static final long serialVersionUID = 5903929353773746851L;
	public ArrayList<OperatorHit> opHits;
	public ArrayList<Integer> wheatTimes;
	public int reward;
	public Long weight;
	
	public Artifact() {
		this.opHits = new ArrayList<OperatorHit>();
	}
	
	public Artifact(ArrayList<OperatorHit> opHits, ArrayList<Integer> wheatTimes, int reward) {
		this.opHits = opHits;
		this.wheatTimes = wheatTimes;
		this.reward = reward;
	}
	
	public int getWheatCount() {
		return this.wheatTimes.size();
	}
	
	public static boolean contains(Artifact pivot, Artifact candidate) {
		// ArrayList<IOperator> doubleRaws = new ArrayList<IOperator>();
		boolean contains = true;
		for (OperatorHit opHitCandidate : candidate.opHits) {
			if (!pivot.opHits.contains(opHitCandidate)) {
				contains = false;
				break;
			}
			/*IOperator doubleRaw = hasDoubleRaw(opHitCandidate);
			if (doubleRaw != null) {
				doubleRaws.add(doubleRaw);
			}*/
		}
		/*if (!contains) {
			boolean containsDouble = false;
			outerloop:
			for (OperatorHit hit : pivot.opHits) {
				for (IOperator doubleRaw : doubleRaws) {
					if (hit.operator.toString().split(doubleRaw.toString()).length > 1) {
						System.out.println(String.format("Found raw=%s in %s", doubleRaw.toString(), hit.operator.toString()));
						containsDouble = true;
						break outerloop;
					}
				}
			}
			return containsDouble;
		}*/
		return contains;
	}
	
	public static IOperator hasDoubleRaw(OperatorHit opHit) {
		IOperator[] ops = opHit.operator.children();
		if (ops[0].equals(ops[1])) {
			return ops[0];
		}
		return null;
	}
	
	// TODO: make unit test
	public static Artifact isRepetition(ArrayList<Artifact> artifacts, Artifact artifact) {
		ArrayList<Artifact> toRemove = new ArrayList<Artifact>();
		for (Artifact artifactCandidate : artifacts) {
			if (artifactCandidate.opHits.size() > artifact.opHits.size()) {
				boolean contains = contains(artifactCandidate, artifact);
				if (contains) {
					toRemove.add(artifactCandidate);
				}
				continue;
			}
			boolean contains = contains(artifact, artifactCandidate);
			if (contains) {
				return artifactCandidate;
			}
		}
		if (toRemove.size() > 0) {
			System.out.println("|Artifact| removing artifacts...");
			artifacts.removeAll(toRemove);
		}
		return null;
	}

	// TODO: optimize as this is an sub-optimal solution
	public static boolean isRepetitionFull(ArrayList<Artifact> artifacts, Artifact artifact) {
		ArrayList<ArrayList<Integer>> indexes = CombinationUtils.getSubsets(artifact.opHits.size());
		
		for (int i = 0; i < indexes.size(); ++i) {
			ArrayList<Integer> subset = indexes.get(i);
			Artifact dummy = new Artifact();
			
			for (int j = 0; j < subset.size(); ++j) {	
				dummy.opHits.add(artifact.opHits.get(subset.get(j)));
			}
			
			Artifact ret = isRepetition(artifacts, dummy);
			
			if (ret != null) {
				if (artifact.opHits.size() < ret.opHits.size()) {
					artifacts.remove(ret);
					return false;
				}
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Artifact [opHits=" + opHits + ", reward=" + reward + "]";
	}
}
