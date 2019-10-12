package plugins.openml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

public class OpenML {
	OpenmlConnector client;

	public void init() {
		this.client = new OpenmlConnector("afd8250e50b774f1cd0b4a4534a1ae90");
	}
	
	public void getDataset() {
		try {
			DataSetDescription data = client.dataGet(62);
			File url = client.datasetGetCsv(data);
			
			ArrayList<HashMap<String, String>> dataset = readCSV(url);
			ValueMapper mapper = new ValueMapper(dataset);
			
			for (String k : dataset.get(0).keySet()) {
				System.out.println(String.format("Feature %s", k));
				mapper.reportUnits(k);
			}
			
			UnitReport report = mapper.getReport();
			
			for (HashMap<String, String> input : dataset) {
				for (String key : input.keySet()) {
					Integer innerValue = report.getInnerValue(key, input.get(key));
					System.out.print(String.format("%s-> %s, ", key, innerValue));
				}
				System.out.println();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
