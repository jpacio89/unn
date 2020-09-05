package unn.structures;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.unn.common.operations.AgentRole;
import com.unn.common.utils.Utils;
import plugins.openml.JobConfig;
import retrofit2.Call;
import retrofit2.Response;
import com.unn.common.server.services.DatacenterService;
import unn.dataset.DatasetLocator;
import unn.dataset.datacenter.DatacenterLocator;
import unn.mining.MiningStatusObservable;
import unn.session.Session;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.MineAction;

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
				HashMap<String, List<String>> options = null;
				DatasetLocator locator = null;
				int attempts = 0;
				do {
					options = self.fetchRandomFeatures();
					locator = new DatacenterLocator(options);
					System.out.println(locator.toString());
				} while(options.size() == 0 && attempts < 10);
				if (options.size() == 0) {
					self.role = null;
					return;
				}
				self.session = new Session(uuid.toString(), self);
				self.session.act(new LoadDatasetAction(self, session, locator));
				JobConfig conf = new JobConfig(role.getTarget().getFeature(), new ArrayList<>());
				self.session.setMineConfig(conf);
				try {
					session.act(new MineAction(session.getMineConfig()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public HashMap<String, List<String>> fetchRandomFeatures() {
		try {
			DatacenterService service = Utils.getDatacenter(true);
			Call<HashMap<String, List<String>>> call = service.getRandomFeatures(role.getLayer());
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
