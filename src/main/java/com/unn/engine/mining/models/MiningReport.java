package com.unn.engine.mining.models;

import com.unn.engine.mining.StatisticsAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;

public class MiningReport {
	public boolean isFinished;
	public HashMap<String, StatisticsAnalyzer> confusionMatrixes;
	public HashMap<String, ArrayList<String>> artifactSignatures;

	public MiningReport() {
		this.confusionMatrixes = new HashMap<String, StatisticsAnalyzer>();
		this.artifactSignatures = new HashMap<String, ArrayList<String>>();
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public HashMap<String, StatisticsAnalyzer> getConfusionMatrixes() {
		return confusionMatrixes;
	}

	public void setConfusionMatrixes(HashMap<String, StatisticsAnalyzer> confusionMatrixes) {
		this.confusionMatrixes = confusionMatrixes;
	}

	public HashMap<String, ArrayList<String>> getArtifactSignatures() {
		return artifactSignatures;
	}

	public void setArtifactSignatures(HashMap<String, ArrayList<String>> artifactSignatures) {
		this.artifactSignatures = artifactSignatures;
	}
}
