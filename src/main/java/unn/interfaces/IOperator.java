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
	
	abstract IOperator cloneStructure ();
	
	abstract IOperator cloneStructureAndData ();

	abstract void getParameters (ArrayList<IOperator> parameters);
	
	abstract void getParametersDirect (ArrayList<IOperator> parameters);
	
	abstract void updateSignature (); 
	
	abstract OperatorDescriptor getDescriptor ();
	
	abstract void setDescriptor (OperatorDescriptor param);

	abstract boolean isBoolean ();
	
	abstract void setParameters(IOperator[] params);
	
	abstract OpIterator iterator ();
	
	abstract void recycle ();
	
	abstract void setTreeId (long treeId);
	
	abstract long getTreeId ();
	
	abstract IOperator[] children ();
	
	abstract String hash ();
	
	// new operations 
	abstract void add_parent (IOperator parent);
	
	abstract ArrayList<IOperator> get_parents ();
	
	abstract boolean isParameter ();
	
	abstract Domain get_domain ();
	
	abstract void set_domain (Domain dom);
	
	abstract IOperator get_time ();
	
	abstract IOperator get_reward ();
	
	abstract void set_value_holder (IOperator op);
	
	abstract IOperator get_value_holder ();
	
	abstract boolean is_value ();
	
	abstract void set_time (IOperator tim);
	
	abstract void set_reward (IOperator rew);
	
	abstract int complexity ();
	
}