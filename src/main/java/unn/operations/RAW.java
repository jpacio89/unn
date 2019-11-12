package unn.operations;

import java.util.ArrayList;

import unn.interfaces.IOperator;
import unn.structures.Config;
import utils.Domain;
import utils.Range;

public class RAW extends BaseOperator
{
	
	public RAW (int v) 
	{
		super ();
		
		define (v);
		
		Domain dom = new Domain ();
		dom.add_range(new Range (v, v));
		
		domain_ = dom;
		set_complexity (0);
	}
	
	public RAW () 
	{
		super ();
		
		init_domain ();
		set_complexity (0);
	}

	public RAW (boolean is_time_reward) 
	{
		super (is_time_reward);
		
		init_domain ();
		set_complexity (0);
	}
	
	private void init_domain ()
	{
		Domain dom = new Domain ();
		dom.add_range(new Range (Config.STIMULI_MIN_VALUE, Config.STIMULI_MAX_VALUE));
		
		domain_ = dom;
	}
	
	public void operate () throws Exception 
	{
	}
	
	@Override
	public IOperator cloneStructure () 
	{
		RAW ret = new RAW ();
		final OperatorDescriptor opParam = this.getDescriptor ();
		ret.setDescriptor(opParam);
		ret.clone_tr_operators (this);
		return ret;
	}

	@Override
	public IOperator cloneStructureAndData() 
	{
		CachedResult cacheRes = this.cloneCachedResult ();
		OperatorDescriptor opParam = this.getDescriptor();
		RAW raw = new RAW ();
		raw.setCachedResult (cacheRes);
		raw.setDescriptor(opParam);
		raw.clone_tr_operators (this);		
		return raw;
	}

	@Override
	public void getParameters (ArrayList<IOperator> parameters) 
	{
		if (isParameter ()) 
		{
			parameters.add (this);
		}
	}
	
	public boolean isParameter ()
	{
		if (!isDefined () && getDescriptor () != null) 
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void getParametersDirect (ArrayList<IOperator> parameters)
	{
		 getParameters (parameters);
	}
	
	@Override
	public boolean isBoolean () 
	{
		return false;
	}
	

	@Override
	public OpIterator iterator () 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setDescriptor (OperatorDescriptor descriptor) 
	{
		super.setDescriptor (descriptor);

		updateSignature ();
	}

	@Override
	public void recycle ()
	{
		if (this.getDescriptor () != null) 
		{
			super.recycle ();
		
			updateSignature ();
		}
	}
	
	@Override
	public String hash ()
	{
		return toString ();
	}
	
	@Override
	public void updateSignature () {
		try {
			String sig = "?";
			
			if (operatorDescriptor_ == null) {
				if (isDefined ()) {
					sig = Integer.toString (value ());
				}
			}
			else {
				sig = operatorDescriptor_.getVtrName ();
			}
			
			cache_.setSignature (sig);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	

	@Override
	public void setParameters (IOperator[] params) 
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public IOperator[] children ()
	{
		return null;
	}
	
}