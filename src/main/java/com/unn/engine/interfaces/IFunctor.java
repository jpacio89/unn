package com.unn.engine.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.functions.FunctionDescriptor;

public interface IFunctor
{
	public interface OpIterator
	{
		abstract IFunctor next ();
	}
	
	abstract Integer operate (HashMap<IFunctor, Integer> values) throws Exception;
	
	abstract int value () throws Exception;
	
	abstract boolean isDefined ();
	
	abstract void define (int v);
	
	abstract void getParameters (ArrayList<IFunctor> parameters);
		
	abstract void updateSignature (); 
	
	abstract FunctionDescriptor getDescriptor ();
	
	abstract void setDescriptor (FunctionDescriptor param);

	abstract boolean isBoolean ();
	
	abstract void setParameters(IFunctor[] params);
	
	abstract OpIterator iterator ();
	
	abstract void recycle ();
	
	abstract IFunctor[] children ();
	
	abstract String hash ();
	
	abstract boolean isParameter ();
}