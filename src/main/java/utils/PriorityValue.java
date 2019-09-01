package utils;

public class PriorityValue {
	int priority_;
	
	public PriorityValue()
	{
		priority_ = 0;
	}
	
	public PriorityValue(int v)
	{
		priority_ = v;
	}
	
	public void setPriority(int v)
	{
		priority_ = v;
	}
	
	public int getPriority()
	{
		return priority_;
	}
		
	public PriorityValue base()
	{
		return this;
	}
}
