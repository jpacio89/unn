package unn.dataset;

import java.util.HashMap;

public class FeatureValueHistogram {
	public double minimum;
	public double maximum;
	public HashMap<Integer, Integer> occurences;
	
	public FeatureValueHistogram() {
		this.occurences = new HashMap<Integer, Integer>();
	}
}
