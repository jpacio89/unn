package com.unn.engine.functions;

import java.io.Serializable;

import com.unn.engine.interfaces.IOperator;

public abstract class BaseFunction implements IOperator, Serializable
{
	private static final long serialVersionUID = 2944349782766148256L;
	Cache cache_;
	FunctionDescriptor functionDescriptor_;
	
	public BaseFunction() {
		init ();
	}
	
	public BaseFunction(boolean is_time_reward) {
		init ();
	}
	
	private void init () {
		cache_ = new Cache();

		updateSignature ();
	}

	public void define (int v) {	
		if (isDefined()) {
			return;
		}
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
	
	public void setCachedResult (Cache cache) {
		this.cache_ = cache;
	}
	
	public BaseFunction base () {
		return (BaseFunction) this;
	}
	
	protected Cache cloneCachedResult () {
		try {
			return (Cache) this.cache_.clone();
		}
		catch (CloneNotSupportedException e) {
			System.err.println("CachedResult - Clone method not supported.");
			return new Cache();
		}
	}
	
	protected void setSignature (String sig) {
		cache_.setSignature (sig);
	}
	
	public FunctionDescriptor getDescriptor () {
		return functionDescriptor_;
	}

	public void setDescriptor (FunctionDescriptor descriptor) {
		this.functionDescriptor_ = descriptor;
	}
	
	public String hash () {
		return this.cache_.getMD5 ();
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
		if (obj instanceof BaseFunction) {
			BaseFunction obj_base = (BaseFunction) obj;
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