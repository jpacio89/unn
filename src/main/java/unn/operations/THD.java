package unn.operations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import unn.interfaces.IOperator;
import unn.structures.Config;

public class THD extends BaseOperator implements IOperator, Serializable
{
	private static final long serialVersionUID = -312207879886911131L;
	IOperator v, lb;

	public THD (IOperator v, IOperator lb) {
		super();
		
		this.v = v;
		this.lb = lb;
		
		updateSignature ();
	}
	
	public THD () {
		super ();
	}
	
	@Override
	public Integer operate(HashMap<IOperator, Integer> values) throws Exception {
		Integer lbV = values.get(lb);
		Integer vV  = values.get(v);
		
		if (lbV == null) {
			if (lb instanceof RAW) {
				lbV = lb.value();
			} else {
				throw new Exception();
			}
		}
		
		if (vV == null) {
			if (v instanceof RAW) {
				vV = v.value();
			} else {
				throw new Exception();
			}
		}
		
		int result = Threshold(lbV, vV, Config.STIMULI_MAX_VALUE - 1);
		return result;
	}


	@Override
	public void getParameters (ArrayList<IOperator> parameters) {
		this.v.getParameters(parameters);
		this.lb.getParameters(parameters);
	}
	
	@Override
	public boolean isBoolean () {
		return true;
	}
	
	@Override
	public void setParameters (IOperator[] params) {
		this.v  = params[0];
		this.lb = params[1];

		updateSignature ();
	}
	
	@Override
	public void updateSignature () {
		String sig = "THD(" + v + "," + lb + ")";
		setSignature (sig);
	}

	@Override
	public OpIterator iterator () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recycle () {
		super.recycle ();
		
		if (v == null || lb == null) {
			return;
		}
		
		v.recycle  ();
		lb.recycle ();
		
		updateSignature ();
	}
	
	@Override
	public IOperator[] children () {
		IOperator[] children = new IOperator[2];
		children[0] = v;
		children[1] = lb;
		
		return children;
	}
	
	@Override
	public boolean isParameter () {
		return false;
	}
	
	public static int Threshold (int v, int lb, int ub) {
		if(v >= lb && v <= ub) {
			return Config.STIMULI_MAX_VALUE;
		}
		return Config.STIMULI_MIN_VALUE;
	}
}
