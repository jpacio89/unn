package unn.dataset.datacenter;

import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import unn.dataset.DatacenterService;
import unn.dataset.OuterDataset;
import unn.structures.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

	DatacenterService getDatacenter() {
		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl(String.format("%s://%s:%d",
				Config.DATACENTER_PROTOCOL,
				Config.DATACENTER_HOST,
				Config.DATACENTER_PORT))
			.addConverterFactory(GsonConverterFactory.create())
			.build();
		DatacenterService service = retrofit.create(DatacenterService.class);
		return service;
	}

	public String fetchDataset(HashMap<String, List<String>> filter) {
		try {
			DatacenterService service = this.getDatacenter();
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
