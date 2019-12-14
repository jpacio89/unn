package unn.operations;

import unn.interfaces.IOperator;

public abstract class BaseOperator implements IOperator
{
	CachedResult cache_;
	OperatorDescriptor operatorDescriptor_;
	
	public BaseOperator () {
		init ();
	}
	
	public BaseOperator (boolean is_time_reward) {
		init ();
	}
	
	private void init () {
		cache_ = new CachedResult ();

		updateSignature ();
	}

	public void define (int v) {	
		cache_.setResult (v);
		updateSignature ();
	}
	
	public boolean isDefined () {
//		return false;
		return cache_.isDefined (); 
	}
	
	public int value () throws Exception {
		return cache_.getResult (); 
	}
	
	public String toString () {
		try {
			return cache_.getSignature ();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return "-";
	}
	
	public void setCachedResult (CachedResult cache) {
		this.cache_ = cache;
	}
	
	public BaseOperator base () {
		return (BaseOperator) this;
	}
	
	protected CachedResult cloneCachedResult () {
		try {
			return (CachedResult) this.cache_.clone();
		}
		catch (CloneNotSupportedException e) {
			System.err.println("CachedResult - Clone method not supported.");
			return new CachedResult ();
		}
	}
	
	protected void setSignature (String sig) {
		cache_.setSignature (sig);
	}
	
	public OperatorDescriptor getDescriptor () {
		return operatorDescriptor_;
	}

	public void setDescriptor (OperatorDescriptor descriptor) {
		this.operatorDescriptor_ = descriptor;
	}
	
	public String hash () {
		return this.cache_.getMD5 ();
	}

	public Integer operate () throws Exception {
		// TODO Auto-generated method stub
		throw new Exception();
	}

	public OpIterator iterator () {
		// TODO Auto-generated method stub
		return null;
	}

	public void recycle () {
		cache_.clear ();
	}

	public int hashCode () {
		try {
			return cache_.getSignature ().hashCode ();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean equals (Object obj) {
		if (obj instanceof BaseOperator) {
			BaseOperator obj_base = (BaseOperator) obj;
			try {
				return obj_base.toString ().equals (cache_.getSignature ());
				//return obj_base == this;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}