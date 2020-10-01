package com.unn.engine.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import com.unn.engine.mining.JobConfig;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.mining.MiningStatusObservable;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.session.Context;
import com.unn.engine.functions.ValueTimeReward;

public class InnerDatasetLoader {
	// TODO: refactor this
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
				System.out.println(String.format("Feature %s", k));
				mapper.reportUnits(k, this.config.groupCount.get(k));
			}
			
			ArrayList<IOperator> leaves = getIdentities(this.config, mapper.getFeatures(), this.config.targetFeature, true);
			this.mapper.setFeatures(this.outerDataset.getHeader().toArray(new String[this.outerDataset.getHeader().size()]));
			
			dataset.setTrainingLeaves(getIdentities(this.config, mapper.getFeatures(), this.config.targetFeature, false));
			dataset.setAllLeaves(leaves);
			int n = 0;
			
			Integer refInnerValue = config.targetInnerValue;
			
			if (config.targetOuterValue != null) {
				refInnerValue = this.mapper.getInnerValue(config.targetFeature, config.targetOuterValue);
			}
			
			int targetFeatureIndex = this.outerDataset.getFeatureIndex(this.config.targetFeature);
			
			if (targetFeatureIndex < 0) {
				return null;
			}
			
			for (int i = 0; i < this.outerDataset.sampleCount(); ++i) {
				if (getStatusObservable() != null) {
					getStatusObservable().updateProgress(n, this.outerDataset.sampleCount());
				}
				
				String outerTargetValue = this.outerDataset.getFeatureAtSample(i, targetFeatureIndex);
				Integer rewardInnerValue = this.mapper.getInnerValue(this.config.targetFeature, outerTargetValue);
				
				rewardInnerValue = JobConfig.mapReward(refInnerValue, rewardInnerValue);
				
				int j = 0;
				for (String key : this.outerDataset.getHeader()) {
					if (this.config.featureBlacklist != null && Arrays.stream(this.config.featureBlacklist).anyMatch(key::equals)) {
						j++;
						continue;
					}
					
					String outerFeatureValue = this.outerDataset.getFeatureAtSample(i, j);
					Integer innerValue = this.mapper.getInnerValue(key, outerFeatureValue);
					ValueTimeReward vtr = new ValueTimeReward(dataset.getFunctorByClassName(key), innerValue, n, rewardInnerValue);
					
					dataset.add(vtr);
					j++;
				}
				
				ValueTimeReward vtr = new ValueTimeReward(dataset.getFunctorByClassName(this.config.targetFeature), rewardInnerValue, n, rewardInnerValue);
				dataset.add(vtr);
				
				n++;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataset;
	}
	
	public static ArrayList<IOperator> getIdentities(JobConfig config, Set<String> features, String rewardFeature, boolean includeReward) {
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		int n = 0;
		for (String feature : features) {
			if (config.featureBlacklist != null &&
				Arrays.stream(config.featureBlacklist).anyMatch(feature::equals)) {
				continue;
			}
			if (feature.equals(rewardFeature)) {
				continue;
			}
    		Raw bop = new Raw();
    		bop.setDescriptor(new FunctionDescriptor(".", feature, n));
    		operators.add(bop);
    		n++;
    	}
		
		if (includeReward) {
			Raw bop = new Raw();
			bop.setDescriptor(new FunctionDescriptor(".", rewardFeature, n));
			operators.add(bop);	
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
