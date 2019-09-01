package unn;

public class VTR {
	Integer v;
	Integer t;
	Integer r;
	IOperator cls;
	
	public VTR(IOperator _cls, Integer _v, Integer _t, Integer _r) {
		this.v = _v;
		this.t = _t;
		this.r = _r;
		this.cls = _cls;
	}
	
	public IOperator getVTRClass() {
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
	
	public VTR setTime(Integer _time) {
		this.t = _time;
		return this;
	}
	
	public VTR setReward(Integer _reward) {
		this.r = _reward;
		return this;
	}
	
	public VTR clone() {
		return new VTR(this.cls, this.v, this.t, this.r);
	}

	@Override
	public String toString() {
		return "VTR [v=" + v + ", t=" + t + ", r=" + r + ", cls=" + cls + "]";
	}
	
	
}
