package unn.mining;

import java.text.DecimalFormat;

import unn.structures.MiningStatus;

public class MiningStatusObservable {
	MiningStatus status;
	
	public MiningStatusObservable() {
		this.status = new MiningStatus();
	}
	
	public void updateArtifactCount(int artifactCount) {
		this.status.artifactCount = artifactCount;
	}
	
	public void updateStatusLabel(String status) {
		this.status.statusLabel = status;
	}
	
	public void updateProgress(long n, long maxN) {
		double percentage = n * 100.0 / maxN;
		DecimalFormat df = new DecimalFormat("#.00");
		status.progressPercentage = Double.parseDouble(df.format(percentage));
	}
	
	public MiningStatus getStatus() {
		return this.status;
	}
}
