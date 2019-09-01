package utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PriorityHashedArray<O extends PriorityValue> {

	HashMap<O, Integer> hashs_;
	ArrayList<O> values_;
	
	public PriorityHashedArray() {
		hashs_ = new HashMap<O, Integer>();
		values_ = new ArrayList<O>();
	}
	
	public void remove(O obj) {
		Integer pos = hashs_.get(obj);
		O ret = values_.get(pos);
		
		if (ret.equals(obj)) {
			values_.remove(pos);
			hashs_.remove(obj);
			
			refreshIndexes(pos);
		}
		else
		{
			System.err.println("[Error] PHA remove not accurate.");
		}
	}
	
	public void add(O obj) {
		int pos = getPos(obj.getPriority());
		
		if (pos >= values_.size()) {
			values_.add(obj);
			pos = values_.size();
		}
		else {
			values_.add(pos, obj);
			refreshIndexes(pos + 1);
		}
		
		hashs_.put(obj, pos);
	}
	
	public void update(O priorityValue) {
		O ob = priorityValue;
		remove(ob);
		add(ob);
	}
	
	@Override
	public String toString() {
		return "PriorityHashedArray [values_=" + values_ + "]";
	}

	private int getPos(int priority) {
		if (values_.size() == 0) {
			return 0;
		}
		
		int lb   = 0;
		int ub   = values_.size() - 1;
		int mid  = getMid(lb, ub);
		int midp = values_.get(mid).getPriority();
		
		while (lb < ub) {			
			if (priority > midp) {
				ub = mid - 1;
			} 
			else if (priority < midp) {
				lb = mid + 1;
			}
			else {
				lb = mid;
				ub = mid;
			}
			
			mid = getMid(lb, ub);
			midp = values_.get(mid).getPriority();			
		}
		
		if (lb == ub) {
			if (priority > midp) {
				return lb;
			}
			else {
				return lb + 1;
			}
		}
		
		return mid;
	}
		
	private int getMid(int lb, int ub) {
		return lb + ((ub-lb) / 2);
	}
	
	private void refreshIndexes(int startPoint) {
		for (int i = startPoint; i < this.values_.size(); ++i) {
			O ob = values_.get(i);
			Integer pos = hashs_.get(ob);
			pos = i;
			
			hashs_.remove(ob);
			hashs_.put(ob, pos);
		}
	}

}
