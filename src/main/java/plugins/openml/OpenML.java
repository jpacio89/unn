package plugins.openml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import unn.Dataset;
import unn.IOperator;
import unn.OperatorDescriptor;
import unn.RAW;
import unn.VTR;

public class OpenML {
	OpenmlConnector client;
	private UnitReport unitReport;

	public void init() {
		this.client = new OpenmlConnector("afd8250e50b774f1cd0b4a4534a1ae90");
	}
	
	public Dataset getDataset(int datasetId) {
		Dataset dataset = new Dataset();
		
		try {
			DataSetDescription data = client.dataGet(datasetId);
			File url = client.datasetGetCsv(data);
			
			ArrayList<HashMap<String, String>> datasetMap = readCSV(url);
			ValueMapper mapper = new ValueMapper(datasetMap);
			
			for (String k : mapper.getFeatures()) {
				System.out.println(String.format("Feature %s", k));
				mapper.reportUnits(k);
			}
			
			ArrayList<IOperator> leaves = getOperators(mapper.getFeatures(), DatasetConfig.className, true);
			UnitReport report = mapper.getReport();
			this.unitReport = report;
			
			dataset.setTrainingLeaves(getOperators(mapper.getFeatures(), DatasetConfig.className, false));
			dataset.setAllLeaves(leaves);
			int n = 0;
			
			for (HashMap<String, String> input : datasetMap) {
				Integer rewardInnerValue = report.getInnerValue(DatasetConfig.className, input.get(DatasetConfig.className));
				rewardInnerValue = DatasetConfig.mapReward(rewardInnerValue);
				
				for (String key : input.keySet()) {
					if (Arrays.stream(DatasetConfig.featureBlacklist).anyMatch(key::equals)) {
						continue;
					}
					Integer innerValue = report.getInnerValue(key, input.get(key));
					System.out.print(String.format("%s-> %s, ", key, innerValue));
					
					VTR vtr = new VTR(dataset.getOperatorByClassName(key), innerValue, n, rewardInnerValue);
					dataset.add(vtr);
				}
				
				VTR vtr = new VTR(dataset.getOperatorByClassName(DatasetConfig.className), rewardInnerValue, n, rewardInnerValue);
				dataset.add(vtr);
				
				System.out.println();
				n++;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dataset;
	}
	
	public static ArrayList<IOperator> getOperators(Set<String> features, String rewardFeature, boolean includeReward) {
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		int n = 0;
		for (String feature : features) {
			if (Arrays.stream(DatasetConfig.featureBlacklist).anyMatch(feature::equals)) {
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
			String[] features = line.split(SEPARATOR);
			
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
