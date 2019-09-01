package utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import unn.Config;

public class Domain 
{
	final Comparator<Range> comparator = new RangeComparator ();
	PriorityQueue<Range> range_list_;
	int count_elements_;
	
	int[] extremes_;
	
	public Domain ()
	{
		count_elements_ = 0;
		range_list_  = new PriorityQueue<Range> (1, comparator);
		extremes_    = new int[2];
		extremes_[0] = Config.STIMULI_MAX_VALUE;
		extremes_[1] = Config.STIMULI_MIN_VALUE;
	}
	
	public int count_elements ()
	{
		count_elements_ = 0;
		Iterator<Range> it = range_list_.iterator ();
		while (it.hasNext ())
		{
			Range rng = it.next ();
			
			count_elements_ += rng.get_ub () - rng.get_lb () + 1;
		}
		
		return count_elements_;
	}
	
	public Integer nearest_boundry (int v)
	{
		int min_dist = Config.STIMULI_MAX_VALUE - Config.STIMULI_MIN_VALUE + 1;
		Integer boundry  = null;
		
		Iterator<Range> it = range_list_.iterator ();
		while (it.hasNext ())
		{
			Range rng = it.next ();
			int d = Math.abs (v - rng.get_lb ());
			if (d < min_dist || boundry == null)
			{
				min_dist = d;
				boundry = rng.get_lb ();
			}
			
			d = Math.abs (v - rng.get_ub ());
			if (d < min_dist || boundry == null)
			{
				min_dist = d;
				boundry = rng.get_ub ();
			}
		}
		
		return boundry;
	}
	
	
	public int get_random_element ()
	{
		count_elements ();
		
		int n = 0;
		int ind = RandomManager.rand (0, count_elements_);
		
		Iterator<Range> it = range_list_.iterator ();
		while (it.hasNext ())
		{
			Range rng = it.next ();
			
			n += rng.get_ub () - rng.get_lb () + 1;
			if (n >= ind)
			{
				return RandomManager.rand (rng.get_lb (), rng.get_ub ());
			}
		}
		
		System.err.println ("Domain - random element not working.");
		return 0;
	}
	
	public int max ()
	{
		return extremes_[1];
	}

	public int min ()
	{
		return extremes_[0];
	}
	
	public boolean univalue ()
	{
		return extremes_[1] - extremes_[0] == 0;
	}
	
	public void add_range (Range range)
	{
		range_list_.add (range);
		extremes_[0] = Math.min (extremes_[0], range.get_lb ());
		extremes_[1] = Math.max (extremes_[1], range.get_ub ());
	}
	
	public PriorityQueue<Range> range_list ()
	{
		return range_list_;
	}
	
	public boolean belongs (int v)
	{
		Iterator<Range> it = range_list_.iterator ();
		
		while (it.hasNext ())
		{
			Range rng = it.next ();
			if (rng.belongs(v))
			{
				return true;
			}
		}
		
		return false;
	}

	public void merge ()
	{
		PriorityQueue<Range> final_domain = new PriorityQueue<Range> (1, comparator);
		PriorityQueue<Range> intermediate = new PriorityQueue<Range> (1, comparator);
		PriorityQueue<Range> cached = range_list_;
		
		while (!cached.isEmpty () && cached.size () > 1)
		{
			intermediate.clear ();
			Range pivot = cached.poll ();
			
			while (!cached.isEmpty ())
			{
				Range element = cached.poll ();
				
				if (element.get_lb () >= pivot.get_lb () && element.get_lb () <= pivot.get_ub ())
				{
					Range new_range = new Range (pivot.get_lb (), Math.max (element.get_ub (), pivot.get_ub ()));
					pivot = new_range;
				}
				else
				{
					intermediate.add (element);
				}
			}
			
			final_domain.add (pivot);
			cached.addAll (intermediate);
		}
		
		while (!cached.isEmpty ())
		{
			final_domain.add(cached.poll ());
		}
		
		range_list_ = final_domain;
		//count_elements ();
	}
	
	public Domain clone ()
	{
		Domain new_dom = new Domain ();
		Iterator<Range> it = range_list_.iterator ();
		
		while (it.hasNext ())
		{
			Range rng = it.next ();
			Range new_range = rng.clone ();
			
			new_dom.add_range (new_range);
		}
		
		return new_dom;
	}
	
	
	
	@Override
	public String toString () 
	{
		PriorityQueue<Range> new_queue = new PriorityQueue<Range> (1, comparator);
		Iterator<Range> it = range_list_.iterator ();
		
		while (it.hasNext ())
		{
			Range rng = it.next ();
			new_queue.add (rng);
		}

		int n = 0;
		String ret = "[";
		while (!new_queue.isEmpty ())
		{
			if (n > 0)
			{
				ret += ",";
			}

			n++;
			ret += new_queue.poll ();
		}
		
		ret += "]";
		return ret;
	}



	public class RangeComparator implements Comparator<Range>
	{
	    @Override
	    public int compare (Range x, Range y)
	    {
	        if (x.get_lb () < y.get_lb ()) 
	        {
	            return -1;
	        }
	        
	        if (x.get_lb () > y.get_lb ()) 
	        {
	            return 1;
	        }
	        
	        return 0;
	    }
	}
}
