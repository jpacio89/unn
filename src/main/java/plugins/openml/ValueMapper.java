package plugins.openml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import unn.dataset.OuterDataset;
import unn.structures.Config;

public class ValueMapper {
	OuterDataset dataset;
	UnitReport report;
	
	public ValueMapper(OuterDataset dataset) {
		this.dataset = dataset;
		this.report = new UnitReport(); 
	}
	
	public void reportUnits(String feature, Integer numericGroupCount) {
		boolean isNumeric = true;
		HashMap<String, String> vals = new HashMap<String, String>();
		ArrayList<Double> numericValues = new ArrayList<Double>();
		
		int featureIndex = this.dataset.getFeatureIndex(feature);
		
		for (int i = 0; i < this.dataset.sampleCount(); ++i) {
			String v = this.dataset.getFeatureAtSample(i, featureIndex);
			
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
		return new HashSet<String>(this.dataset.getHeader());
	}
}
