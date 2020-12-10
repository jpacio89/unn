package com.unn.engine.session;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import com.unn.common.mining.ConfusionMatrix;
import com.unn.common.operations.AgentRole;
import com.unn.common.server.StandardResponse;
import com.unn.common.server.services.DatacenterService;
import com.unn.common.server.services.MaestroService;
import com.unn.common.utils.Utils;
import com.unn.engine.Config;
import com.unn.engine.dataset.datacenter.DatacenterLocator;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.MiningScope;
import com.unn.common.mining.MiningReport;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.session.actions.*;
import retrofit2.Call;
import retrofit2.Response;

public class Session implements Serializable {
	private Context context;
	private OuterDataset outerDataset;
	private HashMap<String, MiningScope> scopes;
	private JobConfig mineConfig;
	private AgentRole role;
	private Thread minerThread;
	private ArrayList<Integer> makerTimes;

	public Session(Context context, AgentRole role) {
		this.scopes = new HashMap<>();
		this.context = context;
		this.role = role;
	}
	
	public void act(Action action) {
		if (action instanceof LoadDatasetAction) {
			LoadDatasetAction __action = (LoadDatasetAction) action;
			__action.setContext(this.context);
			__action.setSession(this);
			__action.act();
			this.outerDataset = __action.getDataset();
		} else if (action instanceof MineAction) {
			MineAction __action = (MineAction) action;
			__action.setConf(this.getMineConfig());
			__action.setSession(this);
			__action.act();
		} else if (action instanceof PublishAction) {
			PublishAction __action = (PublishAction) action;
			__action.setSession(this);
			__action.act();
		}
	}
	
	public OuterDataset getOuterDataset() {
		return outerDataset;
	}
	
	public Context getContext() {
		return this.context;
	}

	public JobConfig getMineConfig() {
		return mineConfig;
	}

	public void setMineConfig(JobConfig mineConfig) {
		this.mineConfig = mineConfig;
	}

	public HashMap<String, MiningScope> getScopes() {
		return this.scopes;
	}

	public MiningReport getReport() {
		MiningReport report = new MiningReport(this.role);
		scopes.entrySet().stream()
			.forEach(entry -> putConfusionMatrix(report, entry));
		return report;
	}

	public AgentRole getRole() {
		return role;
	}

	public boolean isAlive() {
		return this.minerThread != null;
	}

	void processRole() {
		final Session self = this;
		this.minerThread = new Thread(() -> {
			DatasetLocator locator = selectFeatures();
			if (locator != null) {
				mine(locator);
				MiningReport report = getReport();
				if (report == null || report.getConfusionMatrixes().size() == 0) {
					System.out.println("Report statistics --> null");
				} else {
					System.out.println(String.format("Report statistics -->\n%s", report.toString()));
				}
				if (isModelPublishable(report)) {
					publish(locator);
				} else if (this.scopes.size() == 0 || !isLowUnknownRate(report)) {
					// notifyDeadEnd(this.role);
				}
			}
			self.minerThread = null;
		});
		this.minerThread.start();
	}

	private void notifyDeadEnd(AgentRole role) {
		try {
			MaestroService service = Utils.getMaestro();
			Call<StandardResponse> call = service.deadEnd(role);
			call.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isModelPublishable(MiningReport report) {
		return this.scopes.size() > 0 &&
			isLowUnknownRate(report) &&
			isHighAccuracyRate(report);
	}

	private boolean isLowUnknownRate(MiningReport report) {
		return report != null && report.getConfusionMatrixes().values().stream()
			.filter(matrix -> matrix.getUnknownRate() <= Config.MAX_UNKNOWN_RATE)
			.count() > 0;
	}

	private boolean isHighAccuracyRate(MiningReport report) {
		return report != null && report.getConfusionMatrixes().values().stream()
			.filter(matrix -> matrix.getAccuracy() >= Config.MIN_ACCURACY_RATE)
			.count() > 0;
	}

	private void publish(DatasetLocator locator) {
		PublishAction action = new PublishAction();
		action.setUpstreamLayer(role.getLayer());
		action.setDatasetLocator(locator);
		this.act(action);
	}

	private void mine(DatasetLocator locator) {
		try {
			System.out.println("|Context| Loading dataset");
			this.act(new LoadDatasetAction(locator));
			System.out.println("|Context| Starting mining");
			String target = role.getTarget().getFeature().split("@")[0];
			JobConfig conf = new JobConfig(target, new ArrayList<>());
			this.setMineConfig(conf);
			this.act(new MineAction());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DatasetLocator selectFeatures() {
		System.out.println("|Context| Selecting features to mine");
		HashMap<String, List<String>> options = null;
		DatasetLocator locator = null;
		int attempts = 0;
		do {
			options = this.fetchRandomFeatures(role.getTarget().getFeature());
			locator = new DatacenterLocator(options);
			System.out.println(locator.toString());
		} while((options == null || options.size() == 0) && attempts < 10);

		if (options.size() == 0) {
			this.role = null;
			return null;
		}
		return locator;
	}

	public HashMap<String, List<String>> fetchRandomFeatures(String targetFeature) {
		for(int retry = 0; retry < 10; ++retry) {
			try {
				DatacenterService service = Utils.getDatacenter();
				ArrayList<String> whitelist = new ArrayList<>();
				whitelist.add(targetFeature);
				Call<HashMap<String, List<String>>> call = service.getRandomFeatures(role.getLayer(), whitelist);
				Response<HashMap<String, List<String>>> response = call.execute();
				return response.body();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void putConfusionMatrix(MiningReport report, Map.Entry<String, MiningScope> entry) {
		String scopeId = entry.getKey();
		ConfusionMatrix matrix = entry.getValue().getStatisticsAnalyzer().getConfusionMatrix();
		report.confusionMatrixes.put(scopeId, matrix);
	}

	public void setMakerTimes(ArrayList<Integer> times) {
		this.makerTimes = times;
	}

	public ArrayList<Integer> getMakerTimes() {
		return makerTimes;
	}

	public void setOuterDataset(OuterDataset outerDataset) {
		this.outerDataset = outerDataset;
	}
}
