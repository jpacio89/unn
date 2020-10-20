package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.utils.CombinationUtils;

public class Artifact implements Serializable {
	private static final long serialVersionUID = 5903929353773746851L;
	public ArrayList<Portion> opHits;
	public ArrayList<Integer> wheatTimes;
	public int reward;
	public Long weight;
	
	public Artifact() {
		this.opHits = new ArrayList<>();
	}
	
	public Artifact(ArrayList<Portion> opHits, ArrayList<Integer> wheatTimes, int reward) {
		this.opHits = opHits;
		this.wheatTimes = wheatTimes;
		this.reward = reward;
	}
	
	public int getWheatCount() {
		return this.wheatTimes.size();
	}
	
	public static boolean contains(Artifact pivot, Artifact candidate) {
		boolean contains = true;
		for (Portion opHitCandidate : candidate.opHits) {
			if (!pivot.opHits.contains(opHitCandidate)) {
				contains = false;
				break;
			}
		}
		return contains;
	}
	
	// TODO: make unit test
	public static Artifact isRepetition(ArrayList<Artifact> artifacts, Artifact artifact) {
		ArrayList<Artifact> toRemove = new ArrayList<>();
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

	public static class Portion implements Serializable {
		private static final long serialVersionUID = 6076337110545632229L;
		public IFunctor operator;
		public int hit;

		public Portion(IFunctor op, int hit) {
			this.operator = op;
			this.hit = hit;
		}

		@Override
		public String toString() {
			return "OperatorHit [" + operator + " = " + hit + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + hit;
			result = prime * result + ((operator == null) ? 0 : operator.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Portion other = (Portion) obj;
			if (hit != other.hit)
				return false;
			if (operator == null) {
				if (other.operator != null)
					return false;
			} else if (!operator.equals(other.operator))
				return false;
			return true;
		}


	}
}
