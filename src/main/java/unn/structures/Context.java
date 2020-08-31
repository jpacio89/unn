package unn.structures;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import plugins.openml.JobConfig;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import unn.dataset.DatacenterService;
import unn.mining.MiningStatusObservable;

public class Context implements Serializable {
	private static final long serialVersionUID = 329875276429497910L;
	private HashMap<String, MiningStatusObservable> statusObservables;
	private AgentRole role;

	public Context() {
		this.statusObservables = new HashMap<String, MiningStatusObservable>();
	}

	public MiningStatusObservable getStatusObservable(String statusSessionId) {
		return statusObservables.get(statusSessionId);
	}

	public void setStatusObservable(String statusSessionId, MiningStatusObservable statusObservable) {
		this.statusObservables.put(statusSessionId, statusObservable);
	}

	public void registerJobConfig(JobConfig newConfig) {
		setStatusObservable(newConfig.jobSessionId, new MiningStatusObservable());
	}

	public AgentRole getRole() {
		return this.role;
	}

	public void setRole(AgentRole role) {
		this.role = role;
		HashMap<String, List<String>> options = this.fetchRandomFeatures();
		String csv = this.fetchDataset(options);
		// TODO: reset and start mining
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

	public HashMap<String, List<String>> fetchRandomFeatures() {
		try {
			DatacenterService service = this.getDatacenter();
			Call<HashMap<String, List<String>>> call = service.getRandomFeatures(role.layer);
			Response<HashMap<String, List<String>>> response = call.execute();
			return response.body();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
