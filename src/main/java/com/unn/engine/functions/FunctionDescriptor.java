package com.unn.engine.functions;

import java.io.Serializable;

public class FunctionDescriptor implements Serializable {
	private String vtrName;

	public FunctionDescriptor(String vtrName) {
		this.vtrName = vtrName;
	}

	public String getVtrName() {
		return vtrName;
	}

	@Override
	protected Object clone() {
		return new FunctionDescriptor(this.vtrName);
	}
}
