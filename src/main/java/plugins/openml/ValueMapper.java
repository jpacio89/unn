package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import unn.structures.Config;

public class ValueMapper {
	ArrayList<HashMap<String, String>> dataset;
	UnitReport report;
	
	public ValueMapper(ArrayList<HashMap<String, String>> dataset) {
		this.dataset = dataset;
		this.report = new UnitReport(); 
	}
	
	public void reportUnits(String feature, Integer numericGroupCount) {
		boolean isNumeric = true;
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		HashMap<String, String> vals = new HashMap<String, String>();
		ArrayList<Double> numericValues = new ArrayList<Double>();
		
		for (HashMap<String, String> input : this.dataset) {
			String v = input.get(feature);
			
			try {
				double vDouble = Double.parseDouble(v);
				numericValues.add(vDouble);
			} catch (NumberFormatException e) {
				try {
					int vDouble = Integer.parseInt(v);
					numericValues.add((double)vDouble);
				} catch (NumberFormatException e2) {
					isNumeric = false;
				}
			}
			
			vals.put(v, v);
		}
		
		if (isNumeric) {
			System.out.println(String.format("Numeric: %s", feature));
			this.report.addNumeric(feature, numericValues, numericGroupCount);
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
