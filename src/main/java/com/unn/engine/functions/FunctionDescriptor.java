package com.unn.engine.functions;

import java.io.Serializable;

public class FunctionDescriptor implements Serializable {
	private static final long serialVersionUID = -8619446287413861217L;
	private String classId;
	private String vtrName;
	private int vtrIdx;
	
	public FunctionDescriptor(String classId, String vtrName, int vtrIdx) {
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
		return new FunctionDescriptor(this.classId, this.vtrName, this.vtrIdx);
	}
	
	
}