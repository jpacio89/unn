package unn.mining;

import java.util.ArrayList;

public class Artifact {
	public ArrayList<OperatorHit> opHits;
	public ArrayList<Integer> wheatTimes;
	public int reward;
	
	public Artifact(ArrayList<OperatorHit> opHits, ArrayList<Integer> wheatTimes, int reward) {
		this.opHits = opHits;
		this.wheatTimes = wheatTimes;
		this.reward = reward;
	}
	
	public int getWheatCount() {
		return this.wheatTimes.size();
	}
	
	// TODO: make unit test
	public static boolean isRepetition(ArrayList<Artifact> artifacts, Artifact artifact) {
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
				return true;
			}
		}
		return false;
	}
}
