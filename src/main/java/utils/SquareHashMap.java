package utils;

import java.util.ArrayList;
import java.util.HashMap;

public class SquareHashMap<Key1, Key2, Value>
{
	HashMap<Key1, HashMap<Key2, Value>> objects_;
	
	public SquareHashMap ()
	{
		objects_ = new HashMap<Key1, HashMap<Key2, Value>> ();
	}
	
	public boolean containsKey (Key1 key1)
	{
		if (!objects_.containsKey (key1))
		{
			return false;
		}
				
		return true;
	}
	
	public boolean containsKey (Key1 key1, Key2 key2)
	{
		if (!objects_.containsKey (key1))
		{
			return false;
		}
		
		if (!objects_.get (key1).containsKey (key2))
		{
			return false;
		}
		
		return true;
	}
	
	public void remove (Key1 key1, Key2 key2)
	{
		if (containsKey (key1, key2))
		{
			objects_.get (key1).remove (key2);	
		}
	}
	
	public void remove (Key1 key1)
	{
		if (containsKey (key1))
		{
			objects_.remove (key1);	
		}
	}
	
	public void put (Key1 key1, Key2 key2, Value val)
	{
		HashMap<Key2, Value> newEntry = null;
		
		if (containsKey (key1))
		{
			newEntry = objects_.get (key1);
		}
		else
		{
			newEntry = new HashMap<Key2, Value> ();
			objects_.put (key1, newEntry);
		}
		
		newEntry.put (key2, val);
	}
	
	public Value get (Key1 key1, Key2 key2)
	{
		if (containsKey (key1, key2))
		{
			return objects_.get (key1).get (key2);
		}
		
		return null;
	}
	
	public HashMap<Key2, Value> get (Key1 key1)
	{
		if (!containsKey (key1))
		{
			return new HashMap<Key2, Value>();
		}
		
		return objects_.get(key1);
	}
	
	public int size(Key1 key) {
		if (!objects_.containsKey(key)) {
			return 0;
		}
		return objects_.get(key).size();
	}
	
	
}
