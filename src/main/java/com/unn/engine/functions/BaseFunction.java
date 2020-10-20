package com.unn.engine.functions;

import java.io.Serializable;

import com.unn.engine.interfaces.IFunctor;

public abstract class BaseFunction implements IFunctor, Serializable
{
	Cache cache;
	FunctionDescriptor functionDescriptor;
	
	public BaseFunction() {
		this.init ();
	}
	
	private void init () {
		cache = new Cache();
		this.updateSignature();
	}

	public void define (int v) {	
		if (this.isDefined()) {
			return;
		}
		this.cache.setResult(v);
		this.updateSignature();
	}
	
	public boolean isDefined () {
		return this.cache.isDefined ();
	}
	
	public int value () throws Exception {
		return this.cache.getResult ();
	}
	
	public String toString () {
		try {
			return this.cache.getSignature ();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return "-";
	}
	
	protected void setSignature (String sig) {
		this.cache.setSignature (sig);
	}
	
	public FunctionDescriptor getDescriptor () {
		return this.functionDescriptor;
	}

	public void setDescriptor (FunctionDescriptor descriptor) {
		this.functionDescriptor = descriptor;
	}

	public int hashCode () {
		try {
			return this.cache.getSignature().hashCode();
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
				return obj_base.toString ().equals (cache.getSignature ());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}