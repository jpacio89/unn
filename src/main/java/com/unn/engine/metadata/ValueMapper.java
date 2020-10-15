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
		ArrayList<Double> numerics = new ArrayList<>();
		ArrayList<String> labels = new ArrayList<>();
		int featureIndex = this.dataset.getFeatureIndex(feature);

		for (int i = 0; i < this.dataset.sampleCount(); ++i) {
			String v = this.dataset.getFeatureAtSample(i, featureIndex);
			try {
				double vDouble = Double.parseDouble(v);
				numerics.add(vDouble);
			} catch (NumberFormatException e) {
				try {
					int vDouble = Integer.parseInt(v);
					numerics.add((double)vDouble);
				} catch (NumberFormatException e2) {
					labels.add(v);
				}
			}
		}
		
		if (numerics.size() > 0 && labels.size() == 0) {
			System.out.println(String.format("Numeric descriptor", feature));
			this.addNumeric(feature, numerics, numericGroupCount);
		} else if (labels.size() > 0 && numerics.size() == 0) {
			System.out.println(String.format("Discrete descriptor", labels.size()));
			this.addDiscrete(feature, labels);
		} else {
			System.out.println(String.format("Mixed descriptor", feature));
			this.addMixed(feature, labels, numerics, numericGroupCount);
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

	void addDiscrete(String feature, ArrayList<String> values) {
		if (Config.ID.equals(feature)) {
			return;
		}
		// TODO: check this cardinality hack
		final int maxCardinality = 20;
		if (values.size() > maxCardinality) {
			Collections.shuffle(values);
			values = values.stream().limit(maxCardinality)
					.collect(Collectors.toCollection(ArrayList::new));
		}
		Collections.sort(values);
		DiscreteValuesDescriptor descriptor = new DiscreteValuesDescriptor();
		descriptor.init(values);
		this.units.put(feature, descriptor);
	}

	void addNumeric(String feature, ArrayList<Double> values, Integer numericGroupCount) {
		if (Config.ID.equals(feature)) {
			return;
		}
		NumericValuesDescriptor mapper = new NumericValuesDescriptor();
		// TODO: fix group count
		int groupCount = numericGroupCount != null ?
			numericGroupCount : Config.DEFAULT_GROUP_COUNT;
		mapper.init(groupCount, values);
		this.units.put(feature, mapper);
	}

	void addMixed(String feature, ArrayList<String> labels, ArrayList<Double> numerics, Integer numericGroupCount) {
		if (Config.ID.equals(feature)) {
			return;
		}
		MixedValuesDescriptor mapper = new MixedValuesDescriptor();
		// TODO: fix group count
		int groupCount = numericGroupCount != null ?
			numericGroupCount : Config.DEFAULT_GROUP_COUNT;
		mapper.init(groupCount, numerics, labels);
		this.units.put(feature, mapper);
	}
}
