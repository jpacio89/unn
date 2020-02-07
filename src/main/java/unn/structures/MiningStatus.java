package unn.structures;

import java.io.Serializable;

public class MiningStatus implements Serializable {
	private static final long serialVersionUID = 5685497321908960262L;
	public String statusLabel;
	public Integer artifactCount;
	public double progressPercentage;
	
	public MiningStatus() {
		this.artifactCount = 0;
		this.statusLabel = "IDLE";
		this.progressPercentage = 0;
	}
}
