package com.unn.engine.interfaces;

import com.unn.engine.functions.FunctionDescriptor;

public interface IFunctor {
	FunctionDescriptor getDescriptor();
	void setDescriptor(FunctionDescriptor param);
}