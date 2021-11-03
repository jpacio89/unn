package com.unn.engine.dataset;

import java.util.ArrayList;
import java.util.Set;

import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.interfaces.IFeature;

public class InnerDatasetLoader {
	private ValueMapper mapper;
	OuterDataset outerDataset;
	
	public InnerDatasetLoader() {}
	
	public void init(OuterDataset outerDataset) {
		this.outerDataset = outerDataset;
	}
	
	public InnerDataset load () {
		if (outerDataset == null) {
			return null;
		}
		InnerDataset dataset = this.buildDataset();
		return dataset;
	}
	
	private InnerDataset buildDataset() {
		this.mapper = new ValueMapper(this.outerDataset);
		this.mapper.getInputFeatures().stream().forEach((feature) ->
			this.mapper.analyzeValues(feature));
		return Datasets.toInnerDataset(this.outerDataset, this.mapper);
	}

	public static ArrayList<IFeature> getFunctorsByFeatures(ValueMapper mapper) {
		Set<String> features = mapper.getInputFeatures();
		ArrayList<IFeature> operators = new ArrayList<>();
		for (String feature : features) {
			ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(feature);
			if (valuesDescriptor == null) {
				continue;
			}
			for (String group : valuesDescriptor.getOutputFeatures()) {
				IFeature raw = valuesDescriptor.getFeatureByName(group);
				operators.add(raw);
			}
    	}
		return operators;
	}
	
	public ValueMapper getValueMapper() {
		return this.mapper;
	}
}
