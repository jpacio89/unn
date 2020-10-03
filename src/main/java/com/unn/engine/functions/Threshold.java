package com.unn.engine.functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.Config;

public class Threshold extends BaseFunction implements IFunctor, Serializable
{
	private static final long serialVersionUID = -312207879886911131L;
	IFunctor v, lb;

	public Threshold(IFunctor v, IFunctor lb) {
		super();
		
		this.v = v;
		this.lb = lb;
		
		updateSignature ();
	}
	
	public Threshold() {
		super ();
	}
	
	@Override
	public Integer operate(HashMap<IFunctor, Integer> values) throws Exception {
		Integer lbV = values.get(lb);
		Integer vV  = values.get(v);
		
		if (lbV == null) {
			if (lb instanceof Raw) {
				lbV = lb.value();
			} else {
				throw new Exception();
			}
		}
		
		if (vV == null) {
			if (v instanceof Raw) {
				vV = v.value();
			} else {
				throw new Exception();
			}
		}
		
		int result = Threshold(lbV, vV, Config.STIM_MAX - 1);
		return result;
	}


	@Override
	public void getParameters (ArrayList<IFunctor> parameters) {
		this.v.getParameters(parameters);
		this.lb.getParameters(parameters);
	}
	
	@Override
	public boolean isBoolean () {
		return true;
	}
	
	@Override
	public void setParameters (IFunctor[] params) {
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
	public IFunctor[] children () {
		IFunctor[] children = new IFunctor[2];
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
			return Config.STIM_MAX;
		}
		return Config.STIM_MIN;
	}
}
