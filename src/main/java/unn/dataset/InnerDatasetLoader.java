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
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.structures.VTR;

public class InnerDatasetLoader {
	OuterDataset outerDataset;
	JobConfig config;
	
	public InnerDatasetLoader() {}
	
	public void init(JobConfig config, OuterDataset outerDataset) {
		this.outerDataset = outerDataset;
		this.config = config;
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
			this.unitReport.setFeatures(this.features);
			
			dataset.setTrainingLeaves(getOperators(mapper.getFeatures(), this.config.targetFeature, false));
			dataset.setAllLeaves(leaves);
			int n = 0;
			
			Integer refInnerValue = config.targetInnerValue;
			
			if (config.targetOuterValue != null) {
				refInnerValue = report.getInnerValue(config.targetFeature, config.targetOuterValue);
			}
			
			for (HashMap<String, String> input : datasetMap) {
				if (this.statusObservable != null) {
					this.statusObservable.updateProgress(n, datasetMap.size());
				}
				
				Integer rewardInnerValue = report.getInnerValue(this.config.targetFeature, input.get(this.config.targetFeature));
				
				rewardInnerValue = JobConfig.mapReward(refInnerValue, rewardInnerValue);
				
				for (String key : input.keySet()) {
					if (this.config.featureBlacklist != null && Arrays.stream(this.config.featureBlacklist).anyMatch(key::equals)) {
						continue;
					}
					Integer innerValue = report.getInnerValue(key, input.get(key));
					// System.out.print(String.format("%s-> %s, ", key, innerValue));
					
					VTR vtr = new VTR(dataset.getOperatorByClassName(key), innerValue, n, rewardInnerValue);
					dataset.add(vtr);
				}
				
				VTR vtr = new VTR(dataset.getOperatorByClassName(this.config.targetFeature), rewardInnerValue, n, rewardInnerValue);
				dataset.add(vtr);
				
				n++;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
}
