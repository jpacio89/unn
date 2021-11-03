package com.unn.engine.functions;

import com.unn.engine.interfaces.IFeature;

public class ValueTime {
	Integer v;
	Integer t;
	IFeature cls;
	
	public ValueTime(IFeature _cls, Integer _v, Integer _t) {
		this.v = _v;
		this.t = _t;
		this.cls = _cls;
	}
	
	public IFeature getVTRClass() {
		return this.cls;
	}
	
	public Integer getValue() {
		return v;
	}
	
	public Integer getTime() {
		return t;
	}
	
	public ValueTime setTime(Integer _time) {
		this.t = _time;
		return this;
	}
	
	public ValueTime clone() {
		return new ValueTime(this.cls, this.v, this.t);
	}

	@Override
	public String toString() {
		return "VTR [v=" + v + ", t=" + t + ", cls=" + cls + "]";
	}
	
	
}
