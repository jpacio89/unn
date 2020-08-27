package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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

import static spark.Spark.get;
import static spark.Spark.post;

public class RESTApi extends Thread {
	Session session;
	String datasetId;
	int port;
	
	Context unnContext;
	
	public RESTApi(int port) {
		this.port = port;
		this.unnContext = new Context();
	}
	
	public void run() {
		/*Javalin app = Javalin.create(config -> {
            config.enableCorsForOrigin("http://localhost:8080/");
        });
		app.start(this.port);
        app.get("/", ctx -> ctx.result("UNN server running."));*/

		get("/", (request, response) -> {
			return "unn server running";
		});

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
			return "";
		});

		post("/save/session/:jobId", (request, response) -> {
        	// String jobId = request.params("jobId");
        	SaveModelAction action = new SaveModelAction();
        	action.setPathTemplate(String.format("./sessions/%s.session", this.session.getSessionName()));
        	action.setSession(this.session);
        	PersistenceActor saver = new PersistenceActor(action);
        	saver.write();
        	return "";
        });

		post("/load/session/:jobId", (request, response) -> {
			// String jobId = request.params("jobId");
			String name = request.queryParams("name");
        	SaveModelAction action = new SaveModelAction();
        	action.setPathTemplate(String.format("./sessions/%s.session", name));
        	action.setSession(this.session);
        	PersistenceActor saver = new PersistenceActor(action);
        	this.session = saver.read();
        	return "";
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
            ctx.json(sessions);
        });

		get("/dataset/units/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	ctx.json(this.session.getEnv().getUnitReport());
        });

		post("/dataset/mine/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	this.session.setMineConfig(ctx.bodyAsClass(JobConfig.class));
        	
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
        });

		get("/mine/report/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	MiningReport report = this.session.getReport();
			ctx.json(report);
        });

		get("/mine/status/:jobI", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, MiningStatus> statuses = this.session.getMiningStatuses();
			ctx.json(statuses);
        });

		post("/simulate/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	SimulationConfig conf = ctx.bodyAsClass(SimulationConfig.class);
        	SimulationReport report = (SimulationReport) this.session.act(new PredictAction(session, conf));
        	ctx.json(report);
        });

		post("/morph/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	MorphConfig conf = ctx.bodyAsClass(MorphConfig.class);
        	/*MorphReport report = (MorphReport) */this.session.act(new MorphAction(session, conf));
        	//ctx.json(report);
        });

		get("/mine/units/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, UnitReport> reports = this.session.getUnitReports();        	
        	ctx.json(reports);
        });

		get("/mine/config/:jobId", (request, response) -> {
        	ctx.json(this.session.getMineConfig());
        });

		get("/dataset/raw/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	OuterDataset outerDataset = this.session.getOuterDataset();
        	ctx.json(outerDataset);
        });

		get("/feature/histogram/:jobId", (request, response) -> {
        	// String jobId = ctx.pathParam("jobId");
        	String feature = ctx.queryParam("feature");
        	String groupCount = ctx.queryParam("groupCount");
        	
        	JobConfig config = new JobConfig();
        	config.groupCount.put(feature, Integer.parseInt(groupCount));
        	generateUnitReport(config);
        });

		get("/session/features/:sessionId", (request, response) -> {
        	// String jobId = ctx.pathParam("sessionId");
        	ctx.json(this.session.getFeatures());
        });

		get("/session/:sessionId/feedforward/descriptor", (request, response) -> {
        	// TODO: implement
			return "";
        });

		get("/session/:sessionId/feedforward/serve", (request, response) -> {
        	// TODO: implement
			return "";
        });

		post("/session/:sessionId/feedforward/push", (request, response) -> {
        	// TODO: implement
			return "";
        });

		post("/session/:sessionId/feedforward/subscribe", (request, response) -> {
        	// TODO: implement
			return "";
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