package com.unn.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class MultiplesHashMap<Key, Value> implements Serializable
{
	private static final long serialVersionUID = -3874688565215087822L;
	private HashMap<Key, ArrayList <Value>> map_;
	private int array_max_size_;
	
	public MultiplesHashMap ()
	{
		map_ = new HashMap<Key, ArrayList <Value>> ();
		array_max_size_ = 0;
	}
	
	public MultiplesHashMap (int array_max_size)
	{
		map_ = new HashMap<Key, ArrayList <Value>> ();
		array_max_size_ = array_max_size;
	}	
	
	public boolean containsKey (Key key)
	{
		return map_.containsKey (key);
	}
	
	public void remove (Key key)
	{
		map_.remove (key);
	}
	
	public int size ()
	{
		return map_.size ();
	}
	
	public void putAll (Key key, ArrayList<Value> vals)
	{
		for (Value val : vals)
		{
			put (key, val);
		}
	}
	
	public void put (Key key, Value val)
	{
		ArrayList<Value> array = null;
		
		if (!map_.containsKey (key))
		{
			array = new ArrayList<Value> ();
			map_.put (key, array);
		}
		else
		{
			array = map_.get (key);
		}
		
		array.add (val);
		
		if (array.size () > array_max_size_ && array_max_size_ > 0)
		{
			array.remove (0);
		}
	}
	
	public Value get_one (Key key)
	{
		if (!map_.containsKey (key))
		{
			return null;
		}
		
		ArrayList<Value> vals = map_.get (key);
		int idx = RandomManager.rand (0, vals.size () - 1);
		
		return vals.get (idx);
	}
	
	public Key get_one_key ()
	{
		int sz = map_.size ();
		int guess = RandomManager.rand (0, sz - 1);
		
		Iterator<Key> it = map_.keySet().iterator();
		Key k = null;
		
		for (int i = 0; i <= guess; ++i)
		{
			
			k = it.next ();
		}
		
		return k;
	}
	
	public ArrayList<Value> get (Key key)
	{
		if (!map_.containsKey (key))
		{
			return null;
		}
		
		return map_.get (key);
	}

	public void clear ()
	{
		map_.clear ();
	}
	
	public Collection<ArrayList<Value>> values() {
		return this.map_.values();
	}
}
