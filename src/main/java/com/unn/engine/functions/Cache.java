package com.unn.engine.functions;

import java.io.Serializable;

import com.unn.engine.utils.Misc;

class Cache implements Serializable
{
	boolean defined;
	boolean sigDefined;
	int value;
	String signature;
	
	Cache() {
		clear ();
	}
	
	void clear () {
		signature = "";
		value = 0;
		defined = false;
		sigDefined = false;
	}
	
	void setResult (int v) {
		value = v;
		defined = true;
	}
	
	void setSignature (String v) {
		signature = v;
		sigDefined = true;
	}
	
	int getResult () throws Exception {
		if (!defined) {
			throw new Exception(String.format("Result not defined: %s", this.signature));
		}
		return value;
	}
	
	String getSignature () throws Exception {
		if(!sigDefined) {
			throw new Exception("Signature not defined.");
		}
		return signature;
	}
	
	boolean isDefined () {
		return defined;
	}
	
	boolean isSigDefined () {
		return sigDefined;
	}

	@Override
	protected Object clone () {
		Cache res = new Cache();
		try {
			if (this.isDefined ()) {
				res.setResult (getResult ());
			}
			
			if (isSigDefined ()) {
				res.setSignature (new String (this.getSignature ()));
			}
		}
		catch (Exception e) { }
		return res;
	}
}