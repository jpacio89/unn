package unn.structures;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import plugins.openml.JobConfig;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import unn.dataset.DatacenterService;
import unn.dataset.DatasetLocator;
import unn.dataset.datacenter.DatacenterLocator;
import unn.mining.MiningStatusObservable;
import unn.session.Session;
import unn.session.actions.LoadDatasetAction;

public class Context implements Serializable {
	private static final long serialVersionUID = 329875276429497910L;
	private HashMap<String, MiningStatusObservable> statusObservables;
	private AgentRole role;
	private Session session;

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

	public Session getSession() {
		return this.session;
	}

	public void setRole(AgentRole role) {
		this.role = role;
		this.processRole();
	}

	void processRole() {
		final Context self = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO: more user friendly name?
				UUID uuid = UUID.randomUUID();
				HashMap<String, List<String>> options = self.fetchRandomFeatures();
				self.session = new Session(uuid.toString(), self);
				DatasetLocator locator = new DatacenterLocator(options);
				self.session.act(new LoadDatasetAction(self, session, locator));
			}
		}).start();
	}

	public HashMap<String, List<String>> fetchRandomFeatures() {
		try {
			DatacenterService service = Utils.getDatacenter();
			Call<HashMap<String, List<String>>> call = service.getRandomFeatures(role.layer);
			// TODO bug is the response type that mismatches
			Response<HashMap<String, List<String>>> response = call.execute();
			return response.body();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
