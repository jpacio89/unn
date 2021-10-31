package com.unn.engine.interfaces;

import com.unn.engine.functions.FunctionDescriptor;

public interface IFunctor
{
	int value () throws Exception;
	boolean isDefined ();
	void define (int v);
	void updateSignature();
	FunctionDescriptor getDescriptor();
	void setDescriptor(FunctionDescriptor param);
}