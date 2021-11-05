package com.unn.engine;

import java.io.IOException;

import com.google.gson.Gson;

import com.unn.common.globals.NetworkConfig;
import com.unn.common.operations.Agent;
import com.unn.common.operations.AgentRole;
import com.unn.common.operations.DatacenterOrigin;
import com.unn.common.server.StandardResponse;
import com.unn.common.server.StatusResponse;
import com.unn.common.server.services.MaestroService;
import com.unn.common.utils.SparkUtils;
import com.unn.common.utils.Utils;
import com.unn.common.mining.MiningReport;
import com.unn.engine.session.Context;
import com.unn.engine.session.Session;
import retrofit2.Call;
import retrofit2.Response;

import static spark.Spark.*;

public class Server extends Thread {
	static final String SUCCESS = new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
	int port;
	Thread heartbeats;
	Context unnContext;
	Agent myself;

	public Server(int port) {
		this.port = port;
		this.unnContext = new Context();
		this.myself = Config.get().MYSELF.newInstance();
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
		(this.heartbeats = new Thread(() -> {
			for (;;) {
				if (this.session() == null) {
					this.heartbeats = null;
					this.myself = Config.get().MYSELF.newInstance();
					break;
				}

				try {
					MaestroService service = Utils.getMaestro();
					Call<StandardResponse> call = service.heartbeat(this.session().getRole());
					call.execute();
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
	}

	void registerMyself() {
		new Thread(() -> {
			for (;;) {
				try {
					if (this.heartbeats != null) {
						Thread.sleep(1000);
						continue;
					}

					if (this.session() != null) {
						this.startHearbeats();
						Thread.sleep(1000);
						continue;
					}

					System.out.println("|RestServer| Registering myself");
					MaestroService service = Utils.getMaestro();
					Call<StandardResponse> call = service.registerAgent(this.myself);
					call.execute();
					Thread.sleep(1000);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	Session session() {
		return this.unnContext.getLiveSession();
	}
}