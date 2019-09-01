package unn;

public class OperatorDescriptor {
	private String classId;
	private String vtrName;
	private int vtrIdx;
	
	public OperatorDescriptor (String classId, String vtrName, int vtrIdx) {
		this.classId = classId;
		this.vtrName = vtrName;
		this.vtrIdx = vtrIdx;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public String getVtrName() {
		return vtrName;
	}

	public void setVtrName(String vtrName) {
		this.vtrName = vtrName;
	}

	public int getVtrIdx() {
		return vtrIdx;
	}

	public void setVtrIdx(int vtrIdx) {
		this.vtrIdx = vtrIdx;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO: clone Strings - classId and vtrName
		return new OperatorDescriptor(this.classId, this.vtrName, this.vtrIdx);
	}
	
	
}
