package com.unn.engine.dataset.datacenter;

import com.unn.common.server.services.DatacenterService;
import com.unn.common.utils.Utils;
import com.unn.engine.dataset.OuterDataset;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DatacenterDatasetSource {
	HashMap<String, List<String>> options;

	public DatacenterDatasetSource(HashMap<String, List<String>> options) {
		this.options = options;
	}
	
	public OuterDataset next() {
		String csv = fetchDataset(this.options);
		return buildOuterDataset(csv);
	}

	public String fetchDataset(HashMap<String, List<String>> filter) {
		try {
			DatacenterService service = Utils.getDatacenter();
			Call<String> call = service.fetchDataset(filter);
			Response<String> response = call.execute();
			return response.body();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private OuterDataset buildOuterDataset(String csv) {
		final String separator = ",";
		
		try {
			OuterDataset dataset = new OuterDataset();
			String[] lines = csv.split("\n");
			String header = lines[0];
			dataset.setHeader(header.split(separator));
			
			for (int i = 1; i < lines.length; ++i) {
				String line = lines[i];
				
				if (line == null) {
					continue;
				}
				
				String[] sample = line.split(separator);
				
				if (sample.length != dataset.featureCount()) {
					System.err.println(String.format("[OpenMLDatasetProvider] Invalid line: %s", line));
					continue;
				}
				
				dataset.addSample(sample);
			}

			return dataset;
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
