package utils;

public class Range 
{
	int lb_;
	int ub_;
	
	public Range (int lb, int ub)
	{
		lb_ = lb < ub ? lb : ub;
		ub_ = lb < ub ? ub : lb;
	}
	
	public boolean belongs (int val)
	{
		return (val >= lb_ && val <= ub_);
	}
	
	public int get_lb () 
	{
		return lb_;
	}

	public void set_lb (int lb) 
	{
		lb_ = lb;
	}

	public int get_ub () 
	{
		return ub_;
	}

	public void set_ub (int ub) 
	{
		ub_ = ub;
	}
	
	public Range clone ()
	{
		Range new_range = new Range (lb_, ub_);
		return new_range;
	}

	@Override
	public String toString () 
	{
		return "[" + lb_ + ", " + ub_ + "]";
	}	
	
	
}