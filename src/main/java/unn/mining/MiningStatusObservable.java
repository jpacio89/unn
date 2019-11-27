package unn.mining;

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
	
	public MiningStatus getStatus() {
		return this.status;
	}
}
