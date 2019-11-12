package unn.operations;

import java.util.ArrayList;

import unn.interfaces.IOperator;
import unn.structures.Config;
import utils.Domain;
import utils.Range;

public class RAW extends BaseOperator
{
	public RAW (int v) {
		super();
		
		define(v);
		
		Domain dom = new Domain ();
		dom.add_range(new Range (v, v));
		
		domain_ = dom;
	}
	
	public RAW () {
		super ();
		
		init_domain ();
	}

	public RAW (boolean is_time_reward) {
		super (is_time_reward);
		
		init_domain ();
	}
	
	private void init_domain () {
		Domain dom = new Domain ();
		dom.add_range(new Range (Config.STIMULI_MIN_VALUE, Config.STIMULI_MAX_VALUE));
		
		domain_ = dom;
	}

	@Override
	public void getParameters (ArrayList<IOperator> parameters) {
		if (isParameter()) {
			parameters.add (this);
		}
	}
	
	public boolean isParameter () {
		if (!isDefined () && getDescriptor () != null) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isBoolean () {
		return false;
	}
	
	public void setDescriptor (OperatorDescriptor descriptor) {
		super.setDescriptor (descriptor);
		updateSignature ();
	}

	@Override
	public void recycle () {
		if (this.getDescriptor () != null) {
			super.recycle ();
		
			updateSignature ();
		}
	}
	
	@Override
	public String hash () {
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
	public OpIterator iterator () {
		return null;
	}
	
	@Override
	public IOperator[] children () {
		return null;
	}
	
	@Override
	public void setParameters (IOperator[] params) {}
	
	public void operate () throws Exception {}
	
}