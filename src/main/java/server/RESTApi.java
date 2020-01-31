package server;

import java.util.HashMap;

import io.javalin.Javalin;

import plugins.openml.EnvironmentGroup;
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
import unn.session.Session;
import unn.session.actions.LoadAction;
import unn.session.actions.MineAction;
import unn.session.actions.PredictAction;
import unn.simulation.Prediction;
import unn.simulation.SimulationReport;
import unn.structures.Context;
import unn.structures.MiningStatus;

public class RESTApi extends Thread {
	Session session;
	// EnvironmentGroup group;
	IEnvironment env;
	JobConfig mineConfig;
	String datasetId;
	
	Context unnContext;
	
	public RESTApi() {
		this.unnContext = new Context();
	}
	
	public void run() {
		Javalin app = Javalin.create(config -> {
            config.enableCorsForOrigin("http://localhost:8080/");
        });
		app.start(7000);
        app.get("/", ctx -> ctx.result("UNN server running."));
        
        app.post("/dataset/load/:id", ctx -> {
        	this.datasetId = ctx.pathParam("id");
        	
        	// TODO: deprecate group
        	// this.group = new EnvironmentGroup(unnContext, Integer.parseInt(datasetId));
        	this.session = new Session(unnContext);
        	
        	DatasetLocator locator = new OpenMLLocator(Integer.parseInt(datasetId));     
        	//DatasetLocator locator = new FilesystemLocator("/Users/joaocoelho/Documents/Work/UNN/unn-datasets/espiritualidade/espiritualidade.csv");

        	this.session.act(new LoadAction(unnContext, session, locator));
        	
        	generateUnitReport(JobConfig.DEFAULT);
        });
        
        app.get("/dataset/units/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	ctx.json(this.env.getUnitReport());
        });
        
        app.post("/dataset/mine/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	this.mineConfig = ctx.bodyAsClass(JobConfig.class);
        	
    		new Thread(new Runnable() {
				@Override
				public void run() {
		    		try {
		    			session.act(new MineAction(mineConfig));
					}
		    		catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}).start();
        });
        
        app.get("/mine/report/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	MiningReport report = this.session.getReport();
			ctx.json(report);
        });
        
        app.get("/mine/status/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, MiningStatus> statuses = this.session.getMiningStatuses();
			ctx.json(statuses);
        });
        
        app.post("/simulate/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	SimulationConfig conf = ctx.bodyAsClass(SimulationConfig.class);
        	SimulationReport report = (SimulationReport) this.session.act(new PredictAction(session, conf));
        	ctx.json(report);
        });
        
        /*app.post("/morph/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	SimulationConfig conf = ctx.bodyAsClass(SimulationConfig.class);

        	Prediction simulation = new Prediction();
        	simulation.init(conf, this.group);
        	simulation.morph();
        	
        	//ctx.json(report);
        });*/
        
        app.get("/mine/units/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, UnitReport> reports = this.session.getUnitReports();        	
        	ctx.json(reports);
        });
        
        app.get("/mine/config/:jobId", ctx -> {
        	ctx.json(mineConfig);
        });
        
        app.get("/dataset/raw/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	OuterDataset outerDataset = this.session.getOuterDataset();
        	ctx.json(outerDataset);
        });
        
        app.get("/feature/histogram/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	String feature = ctx.queryParam("feature");
        	String groupCount = ctx.queryParam("groupCount");
        	
        	JobConfig config = new JobConfig();
        	config.groupCount.put(feature, Integer.parseInt(groupCount));
        	generateUnitReport(config);
        });
	}
	
	// TODO: refactor this
	// TODO: apply config changes so that it is properly visualized
	private void generateUnitReport(JobConfig config) {
		IEnvironment env = new MiningEnvironment(this.session.getOuterDataset());
		this.env = env;
		env.init(this.unnContext, config);
	}
}
