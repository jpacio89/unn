package unn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import utils.Domain;
import utils.Pair;
import utils.RandomManager;
import utils.Range;

public abstract class BaseOperator implements IOperator
{
	//IOperator parent_, root_;
	ArrayList<IOperator> parents_;
	long treeId_;
	
	CachedResult cache_;
	OperatorDescriptor operatorDescriptor_;
	
	protected Domain domain_;
	
	IOperator value_holder_;
	IOperator timing_;
	IOperator reward_;
	
	int complexity_;
	
	public BaseOperator () 
	{
		init (false);
	}
	
	public BaseOperator (boolean is_time_reward) 
	{
		init (is_time_reward);
	}
	
	protected void set_complexity (int complexity)
	{
		complexity_ = complexity;	
	}
	
	public int complexity ()
	{
		return complexity_;
	}
	
	private void init (boolean is_time_reward)
	{
		treeId_ = -1L;
		cache_ = new CachedResult ();
		parents_ = new ArrayList <IOperator> (); 

		updateSignature ();
		
		if (!is_time_reward)
		{
			load_time_reward ();
		}
	}
	
	private void load_time_reward ()
	{
		timing_ = new RAW (true);
		reward_ = new RAW (true);
		
		timing_.set_value_holder (this);
		reward_.set_value_holder (this);
	}
	
	public void clone_tr_operators (BaseOperator ref)
	{
		if (ref.get_time () != null ||
			ref.get_reward () != null)
		{
			load_time_reward ();
			update_tr_descriptors ();
		}
	}	
	
	protected void update_tr_descriptors ()
	{
		if (timing_ != null)
		{
			timing_.setDescriptor (new OperatorDescriptor (".", this.toString () + ".time", 0));
		}
		
		if (reward_ != null)
		{
			reward_.setDescriptor (new OperatorDescriptor (".", this.toString () + ".reward", 0));
		}
	}
	
	public void set_value_holder (IOperator op)
	{
		value_holder_ = op;
	}
	
	public IOperator get_value_holder ()
	{
		return value_holder_;
	}
	
	public boolean is_value ()
	{
		return value_holder_ == null;
	}
	
	public IOperator get_time ()
	{
		return timing_;
	}
	
	public IOperator get_reward ()
	{
		return reward_;
	}
	
	public void set_time (IOperator tim)
	{
		timing_ = tim;
		timing_.set_value_holder (this);
	}
	
	public void set_reward (IOperator rew)
	{
		reward_ = rew;
		reward_.set_value_holder (this);		
	}
	
	public void define (int v) 
	{	
		cache_.setResult (v);
		
		updateSignature ();
	}
	
	public boolean isDefined () 
	{
//		return false;
		return cache_.isDefined (); 
	}
	
	public int value () throws Exception 
	{
		return cache_.getResult (); 
	}
	
	public String toString () 
	{
		try 
		{
			return cache_.getSignature ();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return "-";
	}
	
	public void setCachedResult (CachedResult cache) 
	{
		this.cache_ = cache;
	}
	
	public BaseOperator base () 
	{
		return (BaseOperator) this;
	}
	
	protected CachedResult cloneCachedResult () 
	{
		try 
		{
			return (CachedResult) this.cache_.clone();
		}
		catch (CloneNotSupportedException e) 
		{
			System.err.println("CachedResult - Clone method not supported.");
			return new CachedResult ();
		}
	}
	
	protected void setSignature (String sig) 
	{
		cache_.setSignature (sig);
	}
	
	public OperatorDescriptor getDescriptor () 
	{
		return operatorDescriptor_;
	}

	public void setDescriptor (OperatorDescriptor descriptor) 
	{
		this.operatorDescriptor_ = descriptor;
		//updateSignature ();
	}
	
	public String hash ()
	{
		return this.cache_.getMD5 ();
	}

	public void operate () throws Exception 
	{
		// TODO Auto-generated method stub
	}

	public OpIterator iterator () 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void recycle () 
	{
		cache_.clear ();
	}

	public void setTreeId (long treeId) 
	{
		treeId_ = treeId;
		
	}

	public long getTreeId () 
	{
		return treeId_;
	}

	public void add_parent (IOperator parent)
	{
		parents_.add (parent);
		
		/*if (parents_.size () == 2)
		{
			restrictor_ = new MultiParentRestrictor (this);
			for (IOperator op : parents_)
			{
				restrictor_.register_parent (op);
			}
		}
		else if (parents_.size () > 2)
		{
			restrictor_.register_parent (parent);
		}*/
	}
	
	public ArrayList<IOperator> get_parents ()
	{
		return parents_;
	}
	
	public Domain get_domain ()
	{
		return domain_;
	}
	
	public void set_domain (Domain dom)
	{
		domain_ = dom;
	}

	public int hashCode ()
	{
		try 
		{
			return cache_.getSignature ().hashCode ();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return 0;
	}

	@Override
	public boolean equals (Object obj) 
	{
		if (obj instanceof BaseOperator)
		{
			BaseOperator obj_base = (BaseOperator) obj;
			try
			{
				return obj_base.toString ().equals (cache_.getSignature ());
				//return obj_base == this;
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static final Comparator<IOperator> VtrIndexComparator = new Comparator<IOperator>() {
	    public int compare(IOperator lhs, IOperator rhs) {
	    	OperatorDescriptor lhsDescriptor = lhs.getDescriptor();
	    	OperatorDescriptor rhsDescriptor = rhs.getDescriptor();
	    	
	        // -1 - less than 
	    	// 1 - greater than
	    	// 0 - equal
	    	// all inverse for descending
	    	if (lhsDescriptor.getVtrIdx() > rhsDescriptor.getVtrIdx()) {
	    		return 1;
	    	} else if (lhsDescriptor.getVtrIdx() < rhsDescriptor.getVtrIdx()) {
	    		return -1;
	    	}
	    	return 0;
	    }
	};
	
	public static final Comparator<IOperator> VtrRandomComparator = new Comparator<IOperator>() {
	    public int compare(IOperator lhs, IOperator rhs) {	    	
	    	int rnd = RandomManager.rand(-1, 1);
	    	return rnd;
	    }
	};
	
	
}