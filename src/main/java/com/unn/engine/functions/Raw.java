package com.unn.engine.functions;

import java.io.Serializable;
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
	
	public boolean isParameter () {
		if (!isDefined () && getDescriptor () != null) {
			return true;
		}
		return false;
	}
	
	public void setDescriptor (FunctionDescriptor descriptor) {
		super.setDescriptor (descriptor);
		updateSignature ();
	}
	
	@Override
	public void updateSignature () {
		try {
			String sig = "?";
			if (functionDescriptor == null) {
				if (isDefined ()) {
					sig = Integer.toString (value ());
				}
			}
			else {
				sig = functionDescriptor.getVtrName ();
			}
			
			cache.setSignature (sig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Integer operate(HashMap<IFunctor, Integer> values) throws Exception {
		throw new Exception();
	}
	
}