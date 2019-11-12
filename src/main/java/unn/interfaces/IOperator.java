package unn.interfaces;

import java.util.ArrayList;

import unn.operations.OperatorDescriptor;
import utils.Domain;

public interface IOperator
{
	public interface OpIterator
	{
		abstract IOperator next ();
	}
	
	abstract void operate () throws Exception;
	
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
	
	abstract Domain get_domain ();
	
	abstract void set_domain (Domain dom);	
}