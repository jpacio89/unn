package com.unn.engine.functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.interfaces.IFunctor;

public class Raw extends BaseFunction implements Serializable
{
	private static final long serialVersionUID = 9162259575884363749L;

	public Raw(int v) {
		super();
		
		define(v);
	}
	
	public Raw() {
		super();
	}

	public Raw(boolean is_time_reward) {
		super (is_time_reward);
	}

	@Override
	public void getParameters (ArrayList<IFunctor> parameters) {
		if (isParameter()) {
			parameters.add (this);
		}
	}
	
	public boolean isParameter () {
		if (!isDefined () && getDescriptor () != null) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isBoolean () {
		return false;
	}
	
	public void setDescriptor (FunctionDescriptor descriptor) {
		super.setDescriptor (descriptor);
		updateSignature ();
	}

	@Override
	public void recycle () {
		if (this.getDescriptor () != null) {
			super.recycle ();
		
			updateSignature ();
		}
	}
	
	@Override
	public String hash () {
		return toString ();
	}
	
	@Override
	public void updateSignature () {
		try {
			String sig = "?";
			
			if (functionDescriptor_ == null) {
				if (isDefined ()) {
					sig = Integer.toString (value ());
				}
			}
			else {
				sig = functionDescriptor_.getVtrName ();
			}
			
			cache_.setSignature (sig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public OpIterator iterator () {
		return null;
	}
	
	@Override
	public IFunctor[] children () {
		return null;
	}
	
	@Override
	public void setParameters (IFunctor[] params) {}

	@Override
	public Integer operate(HashMap<IFunctor, Integer> values) throws Exception {
		throw new Exception();
	}
	
}