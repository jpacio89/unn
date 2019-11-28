package unn.structures;

public class MiningStatus {
	public String statusLabel;
	public Integer artifactCount;
	public double progressPercentage;
	
	public MiningStatus() {
		this.artifactCount = 0;
		this.statusLabel = "IDLE";
		this.progressPercentage = 0;
	}
}
