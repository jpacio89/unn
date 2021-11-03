package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.interfaces.IFeature;
import com.unn.engine.utils.CombinationUtils;

public class Predicate implements Serializable {
	private static final long serialVersionUID = 5903929353773746851L;
	public ArrayList<Integer> targetTimes;
	public ArrayList<Condition> opHits;
	public int reward;
	public Long weight;
	
	public Predicate() {
		this.opHits = new ArrayList<>();
	}
	
	public Predicate(ArrayList<Condition> opHits, int reward, ArrayList<Integer> targetTimes) {
		this.opHits = opHits;
		this.reward = reward;
		this.targetTimes = targetTimes;
	}
	
	public static boolean contains(Predicate pivot, Predicate candidate) {
		boolean contains = true;
		for (Condition opHitCandidate : candidate.opHits) {
			if (!pivot.opHits.contains(opHitCandidate)) {
				contains = false;
				break;
			}
		}
		return contains;
	}
	
	// TODO: make unit test
	public static Predicate isRepetition(ArrayList<Predicate> predicates, Predicate predicate) {
		ArrayList<Predicate> toRemove = new ArrayList<>();
		for (Predicate predicateCandidate : predicates) {
			if (predicateCandidate.opHits.size() > predicate.opHits.size()) {
				boolean contains = contains(predicateCandidate, predicate);
				if (contains) {
					toRemove.add(predicateCandidate);
				}
				continue;
			}
			boolean contains = contains(predicate, predicateCandidate);
			if (contains) {
				return predicateCandidate;
			}
		}
		if (toRemove.size() > 0) {
			System.out.println("|Artifact| removing artifacts...");
			predicates.removeAll(toRemove);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Artifact [opHits=" + opHits + ", reward=" + reward + "]";
	}

	public static class Condition implements Serializable {
		private static final long serialVersionUID = 6076337110545632229L;
		public IFeature operator;
		public int hit;

		public Condition(IFeature op, int hit) {
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
			Condition other = (Condition) obj;
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
