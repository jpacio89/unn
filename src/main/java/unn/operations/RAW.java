package unn.operations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import unn.interfaces.IOperator;

public class RAW extends BaseOperator implements Serializable
{
	private static final long serialVersionUID = 9162259575884363749L;

	public RAW (int v) {
		super();
		
		define(v);
	}
	
	public RAW() {
		super();
	}

	public RAW (boolean is_time_reward) {
		super (is_time_reward);
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

	@Override
	public Integer operate(HashMap<IOperator, Integer> values) throws Exception {
		throw new Exception();
	}
	
}