package unn.operations;

import java.util.ArrayList;

import unn.interfaces.IOperator;
import unn.structures.Config;

public class THD extends BaseOperator implements IOperator
{
	IOperator v, lb;

	public THD (IOperator v, IOperator lb) 
	{
		super();
		
		this.v = v;
		this.lb = lb;
		
		this.v.add_parent  (this);
		this.lb.add_parent (this);
		
		updateSignature ();
		update_tr_descriptors ();
		set_complexity (v.complexity () + lb.complexity () + 1);
	}
	
	public THD () 
	{
		super ();
	}

	public void operate () throws Exception 
	{
		if (!v.isDefined ()) {
			v.operate ();
		}
		
		if (!lb.isDefined ()) {
			lb.operate ();
		}
		
		try {
			int result = PrimaryOperator.Threshold (v.value (), lb.value (), Config.STIMULI_MAX_VALUE - 1);
			this.define (result);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		updateSignature ();
	}

	@Override
	public IOperator cloneStructure () 
	{
		THD thd = new THD ();
		final OperatorDescriptor opParam = this.getDescriptor();
		thd.assignOperatorsStruct (this.v, this.lb);
		thd.setDescriptor(opParam);
		thd.clone_tr_operators (this);
		return thd;
	}

	@Override
	public IOperator cloneStructureAndData ()
	{
		THD thd = new THD ();
		CachedResult cacheRes = this.cloneCachedResult ();
		OperatorDescriptor opParam = this.getDescriptor ();
		thd.assignOperatorsData (this.v, this.lb);
		thd.setCachedResult (cacheRes);
		thd.setDescriptor (opParam);
		thd.clone_tr_operators (this);
		return thd;
	}
	
	private void assignOperatorsStruct (IOperator v, IOperator lb) 
	{
		this.v  = v.cloneStructure ();
		this.lb = lb.cloneStructure ();
		
		set_complexity (v.complexity () + lb.complexity () + 1);
	}
	
	private void assignOperatorsData (IOperator v, IOperator lb) 
	{
		this.v = v.cloneStructureAndData ();
		this.lb = lb.cloneStructureAndData ();
		
		set_complexity (v.complexity () + lb.complexity () + 1);
	}

	@Override
	public void getParameters (ArrayList<IOperator> parameters) 
	{
		this.v.getParameters(parameters);
		this.lb.getParameters(parameters);
	}
	
	@Override
	public void getParametersDirect (ArrayList<IOperator> parameters)
	{
		parameters.add(v);
		parameters.add(lb);
	}	
	
	@Override
	public boolean isBoolean () 
	{
		return true;
	}
	
	@Override
	public void setParameters (IOperator[] params) 
	{
		this.v  = params[0];
		this.lb = params[1];
		
		this.v.add_parent  (this);
		this.lb.add_parent (this);

		updateSignature ();
		
		set_complexity (v.complexity () + lb.complexity () + 1);
	}
	
	@Override
	public void updateSignature ()
	{
		String sig = "THD(" + v + "," + lb + ")";
		setSignature (sig);
		
		//operatorDescriptor_ = new OperatorDescriptor (":", hash (), 0);
	}

	@Override
	public OpIterator iterator () 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recycle ()
	{
		super.recycle ();
		
		if (v == null || lb == null)
		{
			return;
		}
		
		v.recycle  ();
		lb.recycle ();
		
		updateSignature ();
	}
	
	@Override
	public IOperator[] children ()
	{
		IOperator[] children = new IOperator[2];
		children[0] = v;
		children[1] = lb;
		
		return children;
	}
	
	@Override
	public boolean isParameter ()
	{
		return false;
	}	
}
