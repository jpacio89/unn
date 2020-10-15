package com.unn.engine.metadata;

import java.util.*;
import java.util.stream.Collectors;

import com.unn.engine.Config;
import com.unn.engine.dataset.OuterDataset;

public class ValueMapper {
	OuterDataset dataset;
	HashMap<String, ValuesDescriptor> units;

	public ValueMapper(OuterDataset dataset) {
		this.dataset = dataset;
	}
	
	public void reportUnits(String feature, Integer numericGroupCount) {
		boolean isNumeric = true;
		HashMap<String, String> vals = new HashMap<>();
		ArrayList<Double> numericValues = new ArrayList<>();
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
			this.addNumeric(feature, numericValues, numericGroupCount);
		} else {
			System.out.println(String.format("%d discrete values", vals.size()));
			this.addDiscreteSet(feature, new ArrayList<>(vals.keySet()));
		}
	}
	
	public Set<String> getFeatures() {
		return new HashSet<>(this.dataset.getHeader().stream()
			.filter(feature -> !Config.ID.equals(feature))
			.collect(Collectors.toCollection(ArrayList::new)));
	}

	public ValuesDescriptor getValuesDescriptorByFeature(String targetFeature) {
		return this.units.get(targetFeature);
	}

	public void addDiscreteSet(String feature, ArrayList<String> values) {
		// TODO: check this cardinality hack
		final int maxCardinality = 20;
		if (values.size() > maxCardinality) {
			Collections.shuffle(values);
			values = values.stream().limit(maxCardinality)
					.collect(Collectors.toCollection(ArrayList::new));
		}
		if (Config.ID.equals(feature)) {
			return;
		}
		Collections.sort(values);
		this.units.put(feature, new DiscreteValuesDescriptor(values));
	}

	public void addNumeric(String feature, ArrayList<Double> values, Integer numericGroupCount) {
		if (Config.ID.equals(feature)) {
			return;
		}
		NumericValuesDescriptor mapper = new NumericValuesDescriptor();
		// TODO: fix group count
		mapper.init(numericGroupCount != null ? numericGroupCount : Config.DEFAULT_GROUP_COUNT, values);
		this.units.put(feature, mapper);
	}
}
