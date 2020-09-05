package server;

import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import plugins.openml.MiningReport;
import plugins.openml.UnitReport;
import retrofit2.Call;
import retrofit2.Response;
import unn.dataset.MaestroService;
import unn.dataset.datacenter.DatacenterOrigin;
import unn.interfaces.IEnvironment;
import unn.session.Session;
import unn.structures.*;

import static spark.Spark.*;

public class RestServer extends Thread {
	static final String SUCCESS = new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
	int port;
	
	Context unnContext;
	
	public RestServer(int port) {
		this.port = port;
		this.unnContext = new Context();
	}

	Session session() {
		return this.unnContext.getSession();
	}
	
	public void run() {
		port(this.port);
		before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
		get("/", (request, response) -> "unn server running");
		get("/dataset/units", (request, response) -> {
			/*return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, this.session().getEnv().getUnitReport()
			));
			*/
			 return StatusResponse.SUCCESS;
        });
		get("/mine/report", (request, response) -> {
        	// MiningReport report = this.session().getReport();
			/*return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, report
			));*/
			return StatusResponse.SUCCESS;
        });
		get("/mine/status", (request, response) -> {
        	HashMap<String, MiningStatus> statuses = this.session().getMiningStatuses();
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, statuses
			));
        });
		get("/mine/units", (request, response) -> {
        	//HashMap<String, UnitReport> reports = this.session().getUnitReports();
			/*return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, reports
			));*/
			return StatusResponse.SUCCESS;
        });
		get("/mine/config", (request, response) -> {
			/*return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, this.session().getMineConfig()
			));*/
			return StatusResponse.SUCCESS;
        });
		get("/feature/histogram", (request, response) -> {
        	String feature = request.queryParams("feature");
        	String groupCount = request.queryParams("groupCount");
        	JobConfig config = new JobConfig();
        	config.groupCount.put(feature, Integer.parseInt(groupCount));
        	generateUnitReport(config);
			return SUCCESS;
        });
		get("/session/features/:sessionId", (request, response) -> {
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, this.session().getFeatures()
			));
        });
		post("/miner/role", (request, response) -> {
			AgentRole role = new Gson().fromJson(request.body(), AgentRole.class);
			this.unnContext.setRole(role);
			// generateUnitReport(JobConfig.DEFAULT);
			return SUCCESS;
		});

		this.onBooted();
	}

	void onBooted() {
		this.findDatacenter();
		this.registerMyself();
	}

	void findDatacenter() {
		MaestroService service = Utils.getMaestro();
		Call<DatacenterOrigin> call = service.findDatacenter();
		try {
			Response<DatacenterOrigin> response = call.execute();
			DatacenterOrigin origin = response.body();
			Config.DATACENTER_PROTOCOL = origin.getProtocol();
			Config.DATACENTER_HOST = origin.getHost();
			Config.DATACENTER_PORT = origin.getPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void registerMyself() {
		new Thread(() -> {
			for (;;) {
				if (unnContext.getRole() != null) {
					break;
				}
				MaestroService service = Utils.getMaestro();
				Call<StandardResponse> call = service.registerAgent(Config.MYSELF);
				try {
					call.execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	// TODO: refactor this
	// TODO: apply config changes so that it is properly visualized
	private void generateUnitReport(JobConfig config) {
		IEnvironment env = new MiningEnvironment(this.session().getOuterDataset());
		this.session().setEnv(env);
		env.init(this.unnContext, config);
	}
}