package unn.mining;

import java.util.ArrayList;

import utils.CombinationUtils;

public class Artifact {
	public ArrayList<OperatorHit> opHits;
	public ArrayList<Integer> wheatTimes;
	public int reward;
	
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
	
	// TODO: make unit test
	public static Artifact isRepetition(ArrayList<Artifact> artifacts, Artifact artifact) {
		for (Artifact artifactCandidate : artifacts) {
			if (artifactCandidate.opHits.size() != artifact.opHits.size()) {
				continue;
			}
			boolean contains = true;
			for (OperatorHit opHitCandidate : artifactCandidate.opHits) {
				if (!artifact.opHits.contains(opHitCandidate)) {
					contains = false;
					break;
				}
			}
			if (contains) {
				return artifactCandidate;
			}
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
