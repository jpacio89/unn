package unn.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.openml.apiconnector.xml.DataSetDescription;

import plugins.openml.JobConfig;
import plugins.openml.UnitReport;
import plugins.openml.ValueMapper;
import unn.interfaces.IOperator;
import unn.mining.MiningStatusObservable;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.structures.Context;
import unn.structures.VTR;

public class InnerDatasetLoader {
	// TODO: refactor this
	private UnitReport unitReport;
	
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
	
	public InnerDataset buildDataset() {
		InnerDataset dataset = new InnerDataset();
		
		try {
			ValueMapper mapper = new ValueMapper(this.outerDataset);
			
			for (String k : mapper.getFeatures()) {
				System.out.println(String.format("Feature %s", k));
				mapper.reportUnits(k, this.config.groupCount.get(k));
			}
			
			ArrayList<IOperator> leaves = getOperators(mapper.getFeatures(), this.config.targetFeature, true);
			UnitReport report = mapper.getReport();
			this.unitReport = report;
			this.unitReport.setFeatures(this.outerDataset.getHeader().toArray(new String[this.outerDataset.getHeader().size()]));
			
			dataset.setTrainingLeaves(getOperators(mapper.getFeatures(), this.config.targetFeature, false));
			dataset.setAllLeaves(leaves);
			int n = 0;
			
			Integer refInnerValue = config.targetInnerValue;
			
			if (config.targetOuterValue != null) {
				refInnerValue = report.getInnerValue(config.targetFeature, config.targetOuterValue);
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
				Integer rewardInnerValue = report.getInnerValue(this.config.targetFeature, outerTargetValue);
				
				rewardInnerValue = JobConfig.mapReward(refInnerValue, rewardInnerValue);
				
				int j = 0;
				for (String key : this.outerDataset.getHeader()) {
					if (this.config.featureBlacklist != null && Arrays.stream(this.config.featureBlacklist).anyMatch(key::equals)) {
						continue;
					}
					
					String outerFeatureValue = this.outerDataset.getFeatureAtSample(i, j);
					Integer innerValue = report.getInnerValue(key, outerFeatureValue);					
					VTR vtr = new VTR(dataset.getOperatorByClassName(key), innerValue, n, rewardInnerValue);
					
					dataset.add(vtr);
					j++;
				}
				
				VTR vtr = new VTR(dataset.getOperatorByClassName(this.config.targetFeature), rewardInnerValue, n, rewardInnerValue);
				dataset.add(vtr);
				
				n++;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataset;
	}
	
	public ArrayList<IOperator> getOperators(Set<String> features, String rewardFeature, boolean includeReward) {
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		int n = 0;
		for (String feature : features) {
			if (this.config.featureBlacklist != null && Arrays.stream(this.config.featureBlacklist).anyMatch(feature::equals)) {
				continue;
			}
			if (feature.equals(rewardFeature)) {
				continue;
			}
    		RAW bop = new RAW ();
    		bop.setDescriptor(new OperatorDescriptor (".", feature, n));
    		operators.add(bop);
    		n++;
    	}
		
		if (includeReward) {
			RAW bop = new RAW ();
			bop.setDescriptor(new OperatorDescriptor (".", rewardFeature, n));
			operators.add(bop);	
		}
		
		return operators;
	}
	
	private MiningStatusObservable getStatusObservable() {
		return this.context.getStatusObservable(this.config.jobSessionId);
	}
	
	public UnitReport getUnitReport() {
		return this.unitReport;
	}
}
