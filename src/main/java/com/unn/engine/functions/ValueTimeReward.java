package com.unn.engine.functions;

import com.unn.engine.interfaces.IFunctor;

public class ValueTimeReward {
	Integer v;
	Integer t;
	Integer r;
	IFunctor cls;
	
	public ValueTimeReward(IFunctor _cls, Integer _v, Integer _t, Integer _r) {
		this.v = _v;
		this.t = _t;
		this.r = _r;
		this.cls = _cls;
	}
	
	public IFunctor getVTRClass() {
		return this.cls;
	}
	
	public Integer getValue() {
		return v;
	}
	
	public Integer getTime() {
		return t;
	}
	
	public Integer getReward() {
		return r;
	}
	
	public ValueTimeReward setTime(Integer _time) {
		this.t = _time;
		return this;
	}
	
	public ValueTimeReward setReward(Integer _reward) {
		this.r = _reward;
		return this;
	}
	
	public ValueTimeReward clone() {
		return new ValueTimeReward(this.cls, this.v, this.t, this.r);
	}

	@Override
	public String toString() {
		return "VTR [v=" + v + ", t=" + t + ", r=" + r + ", cls=" + cls + "]";
	}
	
	
}
