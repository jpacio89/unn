package unn;

import utils.Misc;

class CachedResult
{
	boolean defined_;
	boolean sigdefined_;
	
	int value_;

	String signature_;
	String md5_signature_;
	
	CachedResult () 
	{
		clear ();
	}
	
	void clear () 
	{
		signature_ = "";
		value_ = 0;
		defined_ = false;
		sigdefined_ = false;
		md5_signature_ = null;
	}
	
	void setResult (int v)
	{
		value_ = v;
		defined_ = true;
	}
	
	void setSignature (String v)
	{
		signature_ = v;
		sigdefined_ = true;
		md5_signature_ = null;
	}		
	
	int getResult () throws Exception 
	{
		if (!defined_) throw new Exception("Result not defined.");
		
		return value_;
	}
	
	String getSignature () throws Exception 
	{
		if(!sigdefined_) throw new Exception("Signature not defined.");
		
		return signature_;
	}
	
	String getMD5 () 
	{
		if(!sigdefined_) return null;
		
		if (md5_signature_ == null) {
			md5_signature_ = ":" + Misc.md5 (signature_);
		}
		
		return md5_signature_;
	}
	
	boolean isDefined ()
	{
		return defined_; 
	}
	
	boolean isSigDefined ()
	{
		return sigdefined_; 
	}

	@Override
	protected Object clone () throws CloneNotSupportedException
	{
		CachedResult res = new CachedResult();
		try
		{
			if (this.isDefined ()) 
			{
				res.setResult (getResult ());
			}
			
			if (isSigDefined ()) 
			{
				res.setSignature (new String (this.getSignature ()));
			}
		} 
		catch (Exception e) 
		{
		}
		
		return res;
	}
}