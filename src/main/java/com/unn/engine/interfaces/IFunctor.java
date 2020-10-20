package com.unn.engine.interfaces;

import java.util.HashMap;
import com.unn.engine.functions.FunctionDescriptor;

public interface IFunctor
{
	Integer operate (HashMap<IFunctor, Integer> values) throws Exception;
	int value () throws Exception;
	boolean isDefined ();
	void define (int v);
	void updateSignature();
	FunctionDescriptor getDescriptor();
	void setDescriptor(FunctionDescriptor param);
	boolean isParameter ();
}