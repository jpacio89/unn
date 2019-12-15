package unn.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import unn.operations.OperatorDescriptor;

public interface IOperator
{
	public interface OpIterator
	{
		abstract IOperator next ();
	}
	
	abstract Integer operate (HashMap<IOperator, Integer> values) throws Exception;
	
	abstract int value () throws Exception;
	
	abstract boolean isDefined ();
	
	abstract void define (int v);
	
	abstract void getParameters (ArrayList<IOperator> parameters);
		
	abstract void updateSignature (); 
	
	abstract OperatorDescriptor getDescriptor ();
	
	abstract void setDescriptor (OperatorDescriptor param);

	abstract boolean isBoolean ();
	
	abstract void setParameters(IOperator[] params);
	
	abstract OpIterator iterator ();
	
	abstract void recycle ();
	
	abstract IOperator[] children ();
	
	abstract String hash ();
	
	abstract boolean isParameter ();
}