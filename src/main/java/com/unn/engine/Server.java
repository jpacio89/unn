package com.unn.engine;

import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

import com.unn.common.globals.NetworkConfig;
import com.unn.common.operations.AgentRole;
import com.unn.common.operations.DatacenterOrigin;
import com.unn.common.server.StandardResponse;
import com.unn.common.server.StatusResponse;
import com.unn.common.server.services.MaestroService;
import com.unn.common.utils.SparkUtils;
import com.unn.common.utils.Utils;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.MiningEnvironment;
import com.unn.engine.mining.MiningReport;
import com.unn.engine.session.Context;
import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.session.Session;
import retrofit2.Call;
import retrofit2.Response;

import static spark.Spark.*;

public class Server extends Thread {
	static final String SUCCESS = new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
	int port;
	Thread heartbeats;
	Context unnContext;
	
	public Server(int port) {
		this.port = port;
		this.unnContext = new Context();
	}

	Session session() {
		return this.unnContext.getSession();
	}
	
	public void run() {
		port(this.port);
		SparkUtils.enableCORS("*","POST, GET, OPTIONS", null);
		get("/", (request, response) -> "com.unn.engine server running");
		get("/mine/report", (request, response) -> {
        	MiningReport report = this.session().getReport();
			return new Gson().toJson(report);
        });
		post("/miner/role", (request, response) -> {
			AgentRole role = new Gson().fromJson(request.body(), AgentRole.class);
			System.out.println(String.format("|RestServer| Received role layer=%d", role.getLayer()));
			this.unnContext.setRole(role);
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
			NetworkConfig.DATACENTER_PROTOCOL = origin.getProtocol();
			NetworkConfig.DATACENTER_HOST = origin.getHost();
			NetworkConfig.DATACENTER_PORT = origin.getPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void startHearbeats() {
		this.heartbeats = new Thread(() -> {
			for(;;) {
				MaestroService service = Utils.getMaestro();
				service.heartbeat(Config.MYSELF);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		this.heartbeats.start();
	}

	void registerMyself() {
		new Thread(() -> {
			for (;;) {
				if (unnContext.getRole() != null) {
					this.startHearbeats();
					break;
				}
				try {
					System.out.println("|RestServer| Registering myself");
					MaestroService service = Utils.getMaestro();
					Call<StandardResponse> call = service.registerAgent(Config.MYSELF);
					call.execute();
					Thread.sleep(1000);
				} catch (IOException e) {
					e.printStackTrace();
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