package com.unn.engine.mining.models;

import java.io.Serializable;
import java.util.ArrayList;

import com.unn.engine.interfaces.IFeature;

public class Predicate implements Serializable {
	private static final long serialVersionUID = 5903929353773746851L;
	public ArrayList<Integer> targetTimes;
	public ArrayList<Condition> conditions;
	public int reward;
	public Long weight;
	
	public Predicate() {
		this.conditions = new ArrayList<>();
	}
	
	public Predicate(ArrayList<Condition> conditions, int reward, ArrayList<Integer> targetTimes) {
		this.conditions = conditions;
		this.reward = reward;
		this.targetTimes = targetTimes;
	}
	
	public static boolean contains(Predicate pivot, Predicate candidate) {
		boolean contains = true;
		for (Condition opHitCandidate : candidate.conditions) {
			if (!pivot.conditions.contains(opHitCandidate)) {
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
			if (predicateCandidate.conditions.size() > predicate.conditions.size()) {
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
		return "Artifact [opHits=" + conditions + ", reward=" + reward + "]";
	}

	public static class Condition implements Serializable {
		private static final long serialVersionUID = 6076337110545632229L;
		public IFeature feature;
		public int activationValue;

		public Condition(IFeature op, int activationValue) {
			this.feature = op;
			this.activationValue = activationValue;
		}

		@Override
		public String toString() {
			return "OperatorHit [" + feature + " = " + activationValue + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + activationValue;
			result = prime * result + ((feature == null) ? 0 : feature.hashCode());
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
			if (activationValue != other.activationValue)
				return false;
			if (feature == null) {
				if (other.feature != null)
					return false;
			} else if (!feature.equals(other.feature))
				return false;
			return true;
		}
	}
}
