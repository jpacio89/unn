package plugins.openml;

import java.util.HashMap;

public class MiningReport {
	public boolean isFinished;
	public HashMap<String, ConfusionMatrix> confusionMatrixes;

	public MiningReport() {}
	
	public class ConfusionMatrix {
		public int[] hits;
		public int unknowns;
		
		public ConfusionMatrix() {}
	}
}
