package plugins.openml;

import java.util.HashMap;

import unn.StatsWalker;

public class MiningReport {
	public boolean isFinished;
	public HashMap<String, StatsWalker> confusionMatrixes;

	public MiningReport() {
		this.confusionMatrixes = new HashMap<String, StatsWalker>();
	}
}
