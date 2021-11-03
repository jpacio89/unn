package com.unn.engine.functions;

import java.io.Serializable;

import com.unn.engine.interfaces.IFunctor;

public class SimpleFunctor implements IFunctor, Serializable {
	private static final long serialVersionUID = 9162259575884363749L;
	FunctionDescriptor descriptor;

	@Override
	public FunctionDescriptor getDescriptor() {
		return this.descriptor;
	}

	public void setDescriptor (FunctionDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public int hashCode() {
		return descriptor.getVtrName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return descriptor.getVtrName().equals(
			((SimpleFunctor)obj).descriptor.getVtrName());
	}
}