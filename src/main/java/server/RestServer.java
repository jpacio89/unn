package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import io.javalin.Javalin;

import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import plugins.openml.MiningReport;
import plugins.openml.SimulationConfig;
import plugins.openml.UnitReport;
import unn.dataset.DatasetLocator;
import unn.dataset.FilesystemLocator;
import unn.dataset.OpenMLLocator;
import unn.dataset.OuterDataset;
import unn.interfaces.IEnvironment;
import unn.morphing.MorphConfig;
import unn.session.Session;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.MineAction;
import unn.session.actions.MorphAction;
import unn.session.actions.PredictAction;
import unn.session.actions.SaveModelAction;
import unn.session.actors.PersistenceActor;
import unn.simulation.SimulationReport;
import unn.structures.Context;
import unn.structures.MiningStatus;
import unn.structures.StandardResponse;
import unn.structures.StatusResponse;

import static spark.Spark.*;

public class RestServer extends Thread {
	static final String SUCCESS = new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
	Session session;
	String datasetId;
	int port;
	
	Context unnContext;
	
	public RestServer(int port) {
		this.port = port;
		this.unnContext = new Context();
	}
	
	public void run() {
		port(8080);
		before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
		get("/", (request, response) -> "unn server running");
		post("/dataset/load/:id", (request, response) -> {
			String name = request.queryParams("name");
			this.datasetId = request.params("id");

			// TODO: deprecate group
			// this.group = new EnvironmentGroup(unnContext, Integer.parseInt(datasetId));
			this.session = new Session(name, unnContext);

			DatasetLocator locator = new OpenMLLocator(Integer.parseInt(datasetId));
			//DatasetLocator locator = new FilesystemLocator("/Users/joaocoelho/Documents/Work/UNN/unn/unn-extras/stock-influencers/etoro.csv");
			//DatasetLocator locator = new FilesystemLocator("/Users/joaocoelho/Documents/Work/UNN/unn/unn-extras/bet-net/data/superbru.csv");

			this.session.act(new LoadDatasetAction(unnContext, session, locator));

			generateUnitReport(JobConfig.DEFAULT);
			return SUCCESS;
		});
		post("/save/session/:jobId", (request, response) -> {
        	// String jobId = request.params("jobId");
        	SaveModelAction action = new SaveModelAction();
        	action.setPathTemplate(String.format("./sessions/%s.session", this.session.getSessionName()));
        	action.setSession(this.session);
        	PersistenceActor saver = new PersistenceActor(action);
        	saver.write();
			return SUCCESS;
        });
		post("/load/session/:jobId", (request, response) -> {
			// String jobId = request.params("jobId");
			String name = request.queryParams("name");
        	SaveModelAction action = new SaveModelAction();
        	action.setPathTemplate(String.format("./sessions/%s.session", name));
        	action.setSession(this.session);
        	PersistenceActor saver = new PersistenceActor(action);
        	this.session = saver.read();
			return SUCCESS;
        });
		get("/list/saved/sessions", (request, response) -> {
        	ArrayList<String> sessions = new ArrayList<String>();
        	File f = new File("./sessions");
            for (String pathname : f.list()) {
            	if (!pathname.endsWith(".session")) {
            		continue;
            	}
            	sessions.add(pathname.replace(".session", ""));
            }
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, sessions
			));
        });
		get("/dataset/units/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, this.session.getEnv().getUnitReport()
			));
        });
		post("/dataset/mine/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
			JobConfig conf = new Gson().fromJson(request.body(), JobConfig.class);
        	this.session.setMineConfig(conf);
    		new Thread(new Runnable() {
				@Override
				public void run() {
		    		try {
		    			session.act(new MineAction(session.getMineConfig()));
					}
		    		catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}).start();
			return SUCCESS;
        });
		get("/mine/report/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	MiningReport report = this.session.getReport();
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, report
			));
        });
		get("/mine/status/:jobI", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, MiningStatus> statuses = this.session.getMiningStatuses();
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, statuses
			));
        });
		post("/simulate/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
			SimulationConfig conf = new Gson().fromJson(request.body(), SimulationConfig.class);
        	SimulationReport report = (SimulationReport) this.session.act(new PredictAction(session, conf));
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, report
			));
        });
		post("/morph/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
			MorphConfig conf = new Gson().fromJson(request.body(), MorphConfig.class);
        	/*MorphReport report = (MorphReport) */this.session.act(new MorphAction(session, conf));
        	//ctx.json(report);
			return SUCCESS;
        });
		get("/mine/units/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, UnitReport> reports = this.session.getUnitReports();
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, reports
			));
        });
		get("/mine/config/:jobId", (request, response) -> {
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, this.session.getMineConfig()
			));
        });
		get("/dataset/raw/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	OuterDataset outerDataset = this.session.getOuterDataset();
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, outerDataset
			));
        });
		get("/feature/histogram/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	String feature = request.queryParams("feature");
        	String groupCount = request.queryParams("groupCount");
        	JobConfig config = new JobConfig();
        	config.groupCount.put(feature, Integer.parseInt(groupCount));
        	generateUnitReport(config);
			return SUCCESS;
        });
		get("/session/features/:sessionId", (request, response) -> {
        	// String jobId = ctx.pathParam("sessionId");
			return new Gson().toJson(new StandardResponse(
				StatusResponse.SUCCESS, null, this.session.getFeatures()
			));
        });
		get("/session/:sessionId/feedforward/descriptor", (request, response) -> {
        	// TODO: implement
			return SUCCESS;
        });
		get("/session/:sessionId/feedforward/serve", (request, response) -> {
        	// TODO: implement
			return SUCCESS;
        });
		post("/session/:sessionId/feedforward/push", (request, response) -> {
        	// TODO: implement
			return SUCCESS;
        });
		post("/session/:sessionId/feedforward/subscribe", (request, response) -> {
        	// TODO: implement
			return SUCCESS;
        });
	}
	
	// TODO: refactor this
	// TODO: apply config changes so that it is properly visualized
	private void generateUnitReport(JobConfig config) {
		IEnvironment env = new MiningEnvironment(this.session.getOuterDataset());
		this.session.setEnv(env);
		env.init(this.unnContext, config);
	}
}