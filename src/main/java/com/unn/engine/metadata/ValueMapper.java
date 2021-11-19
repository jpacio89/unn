package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.unn.common.dataset.Header;
import com.unn.engine.Config;
import com.unn.engine.dataset.OuterDataset;

public class ValueMapper implements Serializable {
	ArrayList<String> datasetHeader;
	long datasetSampleCount;
	transient OuterDataset dataset;
	HashMap<String, ValuesDescriptor> descriptors;

	public ValueMapper(OuterDataset dataset) {
		this.datasetHeader = dataset.getHeader();
		this.datasetSampleCount = dataset.sampleCount();
		this.dataset = dataset;
		this.descriptors = new HashMap<>();
	}
	
	public void analyzeValues(String feature) {
		ArrayList<Double> numerics = new ArrayList<>();
		HashSet<String> labels = new HashSet<>();
		int featureIndex = this.dataset.getFeatureIndex(feature);
		int doubleCount = 0;

		for (int i = 0; i < this.dataset.sampleCount(); ++i) {
			String v = this.dataset.getFeatureAtSample(i, featureIndex);
			try {
				int vInteger = Integer.parseInt(v);
				numerics.add((double) vInteger);
			} catch (NumberFormatException e) {
				try {
					double vDouble = Double.parseDouble(v);
					numerics.add(vDouble);
					doubleCount++;
				} catch (NumberFormatException e2) {
					if (!labels.contains(v)) {
						labels.add(v);
					}
				}
			}
		}

		// NOTE: if feature is only made of Integers
		// (small cardinality) then convert to labels
		if (doubleCount == 0 && numerics.size() > 0) {
			HashSet<Double> set = new HashSet<>(numerics);
			if (set.size() < Config.get().DEFAULT_NUMERIC_CLUSTER_COUNT) {
				ArrayList<String> convertedLabels = set.stream()
						.map((value) -> String.format("labelized_int_%d", Math.round(value)))
						.collect(Collectors.toCollection(ArrayList::new));
				labels.addAll(convertedLabels);
				numerics.clear();
			}
		}
		
		if (numerics.size() > 0 && labels.size() == 0) {
			this.addNumeric(feature, numerics);
		} else if (labels.size() > 0 && numerics.size() == 0) {
			this.addDiscrete(feature, labels);
		} else {
			this.addMixed(feature, labels, numerics);
		}
	}

	public Set<String> getInputFeatures() {
		return new HashSet<>(this.datasetHeader.stream()
			.filter(feature -> !Config.get().ID.equals(feature))
			.collect(Collectors.toCollection(ArrayList::new)));
	}

	public ValuesDescriptor getValuesDescriptorByFeature(String targetFeature) {
		return this.descriptors.get(targetFeature);
	}

	void addDiscrete(String feature, HashSet<String> _labels) {
		if (Config.get().ID.equals(feature) || Config.get().PRIMER.equals(feature)) {
			return;
		}
		if (_labels.size() > this.datasetSampleCount / 2) {
			// NOTE: exluding features with lots of distinct values
			// These features won't produce statistically relevant artifacts
			return;
		}
		ArrayList<String> labels = limitLabels(_labels, Config.get().DEFAULT_DISCRETE_LABEL_COUNT);
		DiscreteValuesDescriptor descriptor = new DiscreteValuesDescriptor();
		descriptor.init(labels);
		this.descriptors.put(feature, descriptor);
		System.out.println(String.format("Discrete descriptor: %s", feature));
		System.out.println("\t" + labels.toString());
	}

	void addNumeric(String feature, ArrayList<Double> values) {
		if (Config.get().ID.equals(feature) || Config.get().PRIMER.equals(feature)) {
			return;
		}
		NumericValuesDescriptor mapper = new NumericValuesDescriptor();
		mapper.init(Config.get().DEFAULT_NUMERIC_CLUSTER_COUNT, values);
		this.descriptors.put(feature, mapper);
		System.out.println(String.format("Numeric descriptor: %s", feature));
	}

	void addMixed(String feature, HashSet<String> _labels, ArrayList<Double> numerics) {
		if (Config.get().ID.equals(feature) || Config.get().PRIMER.equals(feature)) {
			return;
		}
		ArrayList<String> labels = limitLabels(_labels, Config.get().DEFAULT_DISCRETE_LABEL_COUNT / 2);
		MixedValuesDescriptor mapper = new MixedValuesDescriptor();
		int clusterCount = Config.get().DEFAULT_NUMERIC_CLUSTER_COUNT/2 + (Config.get().DEFAULT_DISCRETE_LABEL_COUNT/2 - labels.size());
		mapper.init(clusterCount, numerics, labels);
		this.descriptors.put(feature, mapper);
		System.out.println(String.format("Mixed descriptor: %s", feature));
		System.out.println("\t" + labels.toString());
	}

	ArrayList<String> limitLabels(HashSet<String> labels, int size) {
		ArrayList<String> values = labels.stream()
			.collect(Collectors.toCollection(ArrayList::new));
		if (values.size() > size) {
			Collections.shuffle(values);
			values = values.stream()
				.limit(size)
				.collect(Collectors.toCollection(ArrayList::new));
		}
		return values;
	}

	public void setDataset(OuterDataset dataset) {
		this.dataset = dataset;
	}
}
