package plugins.openml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import unn.dataset.Dataset;
import unn.interfaces.IOperator;
import unn.mining.MiningStatusObservable;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.structures.VTR;

public class OpenML {
	OpenmlConnector client;
	private UnitReport unitReport;
	private String[] features;
	private JobConfig config;
	private MiningStatusObservable statusObservable;

	public void init(JobConfig config, MiningStatusObservable statusObservable) {
		this.config = config;
		this.client = new OpenmlConnector("afd8250e50b774f1cd0b4a4534a1ae90");
		this.statusObservable = statusObservable;
	}
	
	public ArrayList<HashMap<String, String>> getRawDataset(int datasetId) {
		try {
			DataSetDescription data = client.dataGet(datasetId);
			File url = client.datasetGetCsv(data);
			//File url = new File("/Users/joaocoelho/Documents/Work/UNN/unn/unn-engine/php/dataset/dataset-merge.csv");
			
			ArrayList<HashMap<String, String>> datasetMap = readCSV(url);
			return datasetMap;
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Dataset getDataset(int datasetId) {
		Dataset dataset = new Dataset();
		
		try {
			DataSetDescription data = client.dataGet(datasetId);
			File url = client.datasetGetCsv(data);
			//File url = new File("/Users/joaocoelho/Documents/Work/UNN/unn/unn-engine/php/dataset/dataset-merge.csv");
			
			ArrayList<HashMap<String, String>> datasetMap = readCSV(url);
			ValueMapper mapper = new ValueMapper(datasetMap);
			
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
	
	public ArrayList<HashMap<String, String>> readCSV(File csv) {
		String SEPARATOR = ",";
		ArrayList<HashMap<String, String>> dataset = new  ArrayList<HashMap<String, String>>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(csv));
			String line = reader.readLine();
			this.features = line.split(SEPARATOR);

			while (line != null) {
				line = reader.readLine();
				if (line == null) {
					continue;
				}
				String[] values = line.split(SEPARATOR);
				
				if (values.length != features.length) {
					System.err.println(String.format("Invalid line: %s", line));
					continue;
				}
				
				HashMap<String, String> input = new HashMap<String, String>();
				
				for (int i = 0; i < values.length; ++i) {
					input.put(features[i], values[i]);
				}
				
				dataset.add(input);
			}
			
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return dataset;
	}

	public UnitReport getUnitReport() {
		return this.unitReport;
	}
}
