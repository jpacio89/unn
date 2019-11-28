package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;

import unn.mining.StatsWalker;

public class MiningReport {
	public boolean isFinished;
	public HashMap<String, StatsWalker> confusionMatrixes;
	public HashMap<String, ArrayList<String>> artifactSignatures;

	public MiningReport() {
		this.confusionMatrixes = new HashMap<String, StatsWalker>();
		this.artifactSignatures = new HashMap<String, ArrayList<String>>();
	}
}
