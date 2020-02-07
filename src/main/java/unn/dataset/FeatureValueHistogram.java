package unn.dataset;

import java.io.Serializable;
import java.util.HashMap;

public class FeatureValueHistogram implements Serializable {
	private static final long serialVersionUID = 6859083898627915999L;
	public double minimum;
	public double maximum;
	public HashMap<Integer, Integer> occurences;
	
	public FeatureValueHistogram() {
		this.occurences = new HashMap<Integer, Integer>();
	}
}
