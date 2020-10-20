package com.unn.engine.dataset;

import java.util.ArrayList;
import java.util.Set;

import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.mining.models.MiningStatusObservable;
import com.unn.engine.session.Context;

public class InnerDatasetLoader {
	private ValueMapper mapper;
	OuterDataset outerDataset;
	JobConfig config;
	Context context;
	
	public InnerDatasetLoader() {}
	
	public void init(Context context, JobConfig config, OuterDataset outerDataset) {
		this.outerDataset = outerDataset;
		this.config = config;
		this.context = context;
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
		this.mapper.getFeatures().stream().forEach((feature) ->
			this.mapper.analyzeValues(feature));
		return Datasets.toInnerDataset(this.outerDataset, this.mapper);
	}

	public static ArrayList<IFunctor> getFunctorsByFeatures(ValueMapper mapper) {
		Set<String> features = mapper.getFeatures();
		ArrayList<IFunctor> operators = new ArrayList<>();
		for (String feature : features) {
			ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(feature);
			if (valuesDescriptor == null) {
				continue;
			}
			for (String group : valuesDescriptor.getGroups(feature)) {
				IFunctor raw = valuesDescriptor.getFunctorByGroup(group);
				operators.add(raw);
			}
    	}
		return operators;
	}
	
	private MiningStatusObservable getStatusObservable() {
		if (this.context == null) {
			return null;
		}
		return this.context.getStatusObservable(this.config.jobSessionId);
	}
	
	public ValueMapper getValueMapper() {
		return this.mapper;
	}
}
