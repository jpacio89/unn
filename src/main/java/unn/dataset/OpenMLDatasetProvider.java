package unn.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

public class OpenMLDatasetProvider {
	final String apiKey = "afd8250e50b774f1cd0b4a4534a1ae90";
	
	OpenMLLocator locator;
	OpenmlConnector client;
	
	public OpenMLDatasetProvider(DatasetLocator locator) {
		this.locator = (OpenMLLocator) locator;
	}
	
	public void init() {
		this.client = new OpenmlConnector(apiKey);
	}

	public OuterDataset load() {
		try {
			DataSetDescription data = this.client.dataGet(locator.getDatasetId());
			File csv = this.client.datasetGetCsv(data);
			//File url = new File("/Users/joaocoelho/Documents/Work/UNN/unn/unn-extras/wav-analyzer/dataset.csv");
			
			// TODO: assign to OuterDataset
			readCSV(csv);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
}
