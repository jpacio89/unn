package com.unn.engine.functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.Config;

public class Threshold extends BaseFunction implements IFunctor, Serializable
{
	IFunctor v, lb;

	public Threshold(IFunctor v, IFunctor lb) {
		super();
		this.v = v;
		this.lb = lb;
		this.updateSignature ();
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
	public void updateSignature () {
		String sig = "THD(" + v + "," + lb + ")";
		setSignature (sig);
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
