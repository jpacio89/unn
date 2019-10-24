package plugins.openml;

import java.util.ArrayList;

public class DiscreteSet extends OuterValueType {
	public final ArrayList<String> values;
	
	public DiscreteSet(ArrayList _values) {
		this.values = _values;
	}
	
	public Integer getIndex(String _value) {
		int index = this.values.indexOf(_value);
		if (index < 0) {
			return null;
		}
		return index;
	}
	
	public int cardinal() {
		return this.values.size();
	}
}