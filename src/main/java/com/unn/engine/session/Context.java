package com.unn.engine.session;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.unn.common.operations.AgentRole;
import com.unn.common.server.services.DatacenterService;
import com.unn.common.utils.Utils;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.mining.MiningStatusObservable;
import com.unn.engine.session.actions.LoadDatasetAction;
import com.unn.engine.session.actions.MineAction;
import com.unn.engine.mining.JobConfig;
import retrofit2.Call;
import retrofit2.Response;
import com.unn.engine.dataset.datacenter.DatacenterLocator;

public class Context implements Serializable {
	private static final long serialVersionUID = 329875276429497910L;
	private HashMap<String, MiningStatusObservable> statusObservables;
	private AgentRole role;
	private Session session;
	private Thread minerThread;

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
		this.minerThread = new Thread(() -> {
			DatasetLocator locator = loadDataset();
			if (locator != null) {
				mine(locator);
			}
			self.minerThread = null;
		});
		this.minerThread.start();
	}

	private boolean isModelPublishable() {
		return true;
	}

	private void pushPredictions() {

	}

	private void mine(DatasetLocator locator) {
		System.out.println("|Context| Preparing to mine");
		UUID uuid = UUID.randomUUID();
		this.session = new Session(uuid.toString(), this);
		this.session.act(new LoadDatasetAction(this, session, locator));
		JobConfig conf = new JobConfig(role.getTarget().getFeature().split("@")[0], new ArrayList<>());
		this.session.setMineConfig(conf);
		try {
			session.act(new MineAction(session.getMineConfig()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DatasetLocator loadDataset() {
		System.out.println("|Context| Loading dataset");
		HashMap<String, List<String>> options = null;
		DatasetLocator locator = null;
		int attempts = 0;

		do {
			options = this.fetchRandomFeatures(role.getTarget().getFeature());
			locator = new DatacenterLocator(options);
			System.out.println(locator.toString());
		} while(options.size() == 0 && attempts < 10);

		if (options.size() == 0) {
			this.role = null;
			return null;
		}
		return locator;
	}

	public HashMap<String, List<String>> fetchRandomFeatures(String targetFeature) {
		try {
			DatacenterService service = Utils.getDatacenter(true);
			ArrayList<String> whitelist = new ArrayList<>();
			whitelist.add(targetFeature);
			Call<HashMap<String, List<String>>> call = service.getRandomFeatures(role.getLayer(), whitelist);
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
