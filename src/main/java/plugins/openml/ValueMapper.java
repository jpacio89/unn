package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ValueMapper {
	ArrayList<HashMap<String, String>> dataset;
	UnitReport report;
	
	public ValueMapper(ArrayList<HashMap<String, String>> dataset) {
		this.dataset = dataset;
		this.report = new UnitReport(); 
	}
	
	public void reportUnits(String feature) {
		boolean isNumeric = true;
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		HashMap<String, String> vals = new HashMap<String, String>();
		
		for (HashMap<String, String> input : this.dataset) {
			String v = input.get(feature);
			
			try {
				double vDouble = Double.parseDouble(v);					
				minVal = Math.min(minVal, vDouble);
				maxVal = Math.max(maxVal, vDouble);
			} catch (NumberFormatException e) {
				try {
					int vDouble = Integer.parseInt(v);					
					minVal = Math.min(minVal, vDouble);
					maxVal = Math.max(maxVal, vDouble);
				} catch (NumberFormatException e2) {
					isNumeric = false;
				}
			}
			
			vals.put(v, v);
		}
		
		if (isNumeric) {
			System.out.println(String.format("Numeric: [%f; %f]", minVal, maxVal));
			this.report.addRange(feature, minVal, maxVal);
		} else {
			System.out.println(String.format("%d discrete values", vals.size()));
			this.report.addDiscreteSet(feature, new ArrayList<String>(vals.keySet()));
		}
	}
	
	public UnitReport getReport() {
		return this.report;
	}
	
	public Set<String> getFeatures() {
		return this.dataset.get(0).keySet();
	}
}
