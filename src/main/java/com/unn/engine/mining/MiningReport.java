package com.unn.engine.mining;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.mining.StatsWalker;

public class MiningReport {
	public boolean isFinished;
	public HashMap<String, StatsWalker> confusionMatrixes;
	public HashMap<String, ArrayList<String>> artifactSignatures;

	public MiningReport() {
		this.confusionMatrixes = new HashMap<String, StatsWalker>();
		this.artifactSignatures = new HashMap<String, ArrayList<String>>();
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public HashMap<String, StatsWalker> getConfusionMatrixes() {
		return confusionMatrixes;
	}

	public void setConfusionMatrixes(HashMap<String, StatsWalker> confusionMatrixes) {
		this.confusionMatrixes = confusionMatrixes;
	}

	public HashMap<String, ArrayList<String>> getArtifactSignatures() {
		return artifactSignatures;
	}

	public void setArtifactSignatures(HashMap<String, ArrayList<String>> artifactSignatures) {
		this.artifactSignatures = artifactSignatures;
	}
}
