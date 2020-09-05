package unn.dataset.datacenter;

import unn.dataset.DatasetLocator;

import java.util.HashMap;
import java.util.List;

public class DatacenterLocator extends DatasetLocator {
	HashMap<String, List<String>> options;

	public DatacenterLocator() {}

	public DatacenterLocator(HashMap<String, List<String>> path) {
		this.options = path;
	}

	public HashMap<String, List<String>> getOptions() {
		return options;
	}

	public void setOptions(HashMap<String, List<String>> options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return "DatacenterLocator {\n" +
				"\toptions=" + options +
				"\n}";
	}
}
