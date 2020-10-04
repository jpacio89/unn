package com.unn.engine.dataset;

import java.util.ArrayList;
import java.util.Set;

import com.unn.engine.Config;
import com.unn.engine.metadata.ValuesDescriptor;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.mining.MiningStatusObservable;
import com.unn.engine.session.Context;
import com.unn.engine.functions.ValueTimeReward;

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
		InnerDataset dataset = new InnerDataset();
		
		try {
			this.mapper = new ValueMapper(this.outerDataset);

			for (String k : mapper.getFeatures()) {
				mapper.reportUnits(k, this.config.groupCount.get(k));
			}

			ArrayList<IFunctor> leaves = getFunctorsByFeatures(this.mapper);
			this.mapper.setFeatures(this.outerDataset.getHeader().toArray(new String[this.outerDataset.getHeader().size()]));
			dataset.setFunctors(getFunctorsByFeatures(this.mapper));

			int n = 0;
			for (int i = 0; i < this.outerDataset.sampleCount(); ++i) {
				if (getStatusObservable() != null) {
					getStatusObservable().updateProgress(n, this.outerDataset.sampleCount());
				}
				int j = 0;
				for (String key : this.outerDataset.getHeader()) {
					String outerFeatureValue = this.outerDataset.getFeatureAtSample(i, j);
					ValuesDescriptor valuesDescriptor = this.mapper.getValuesDescriptorByFeature(key);
					String featureGroup = valuesDescriptor.getGroupByOuterValue(outerFeatureValue);
					for (String group : valuesDescriptor.getGroups()) {
						Integer v = featureGroup.equals(group) ?
							Config.STIM_MAX : Config.STIM_MIN;
						IFunctor op = valuesDescriptor.getFunctorByGroup(group);
						ValueTimeReward vtr = new ValueTimeReward(op, v, n, null);
						dataset.add(vtr);
					}
					j++;
				}
				n++;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataset;
	}

	public static ArrayList<IFunctor> getFunctorsByFeatures(ValueMapper mapper) {
		Set<String> features = mapper.getFeatures();
		ArrayList<IFunctor> operators = new ArrayList<>();
		for (String feature : features) {
			ValuesDescriptor valuesDescriptor = mapper.getValuesDescriptorByFeature(feature);
			for (String group : valuesDescriptor.getGroups()) {
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
