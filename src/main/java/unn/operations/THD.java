package unn.operations;

import java.util.ArrayList;

import unn.interfaces.IOperator;
import unn.structures.Config;

public class THD extends BaseOperator implements IOperator
{
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

	public void operate () throws Exception {
		if (!v.isDefined ()) {
			v.operate ();
		}
		
		if (!lb.isDefined ()) {
			lb.operate ();
		}
		
		try {
			int result = PrimaryOperator.Threshold (v.value (), lb.value (), Config.STIMULI_MAX_VALUE - 1);
			this.define (result);
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		
		updateSignature ();
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
}
