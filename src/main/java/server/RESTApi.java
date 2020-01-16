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
import unn.dataset.OpenMLLocator;
import unn.dataset.OuterDataset;
import unn.interfaces.IEnvironment;
import unn.session.Session;
import unn.session.actions.LoadAction;
import unn.simulation.Simulation;
import unn.simulation.SimulationReport;
import unn.structures.Context;
import unn.structures.MiningStatus;

public class RESTApi extends Thread {
	Session session;
	EnvironmentGroup group;
	IEnvironment env;
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
        	this.group = new EnvironmentGroup(unnContext, Integer.parseInt(datasetId));
        	this.session = new Session(unnContext);
        	
        	DatasetLocator locator = new OpenMLLocator(Integer.parseInt(datasetId));        	
        	this.session.act(new LoadAction(locator));
        	
        	// TODO: fix hardcoded openml dataset id
        	this.group.load(locator);
        	
        	// TODO: remove this
    		IEnvironment env = new MiningEnvironment(this.group.getOuterDataset());
    		this.env = env;
			env.init(this.unnContext, JobConfig.DEFAULT);
        });
        
        app.get("/dataset/units/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	ctx.json(this.env.getUnitReport());
        });
        
        app.post("/dataset/mine/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	JobConfig conf = ctx.bodyAsClass(JobConfig.class);
        	
    		new Thread(new Runnable() {
				@Override
				public void run() {
		    		try {
		    			group.mine(conf);
					}
		    		catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}).start();
        });
        
        app.get("/mine/report/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	// IEnvironment env = this.env;
        	MiningReport report = group.getReport();
			ctx.json(report);
        });
        
        app.get("/mine/status/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, MiningStatus> statuses = group.getMiningStatuses();
			ctx.json(statuses);
        });
        
        app.post("/simulate/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	SimulationConfig conf = ctx.bodyAsClass(SimulationConfig.class);

        	Simulation simulation = new Simulation();
        	simulation.init(conf, this.group);
        	simulation.run();
        	SimulationReport report = simulation.getReport();
        	
        	ctx.json(report);
        });
        
        app.post("/morph/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	SimulationConfig conf = ctx.bodyAsClass(SimulationConfig.class);

        	Simulation simulation = new Simulation();
        	simulation.init(conf, this.group);
        	simulation.morph();
        	// SimulationReport report = simulation.getReport();
        	
        	//ctx.json(report);
        });
        
        app.get("/mine/units/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	HashMap<String, UnitReport> reports = this.group.getUnitReports();        	
        	ctx.json(reports);
        });
        
        app.get("/mine/config/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	JobConfig config = this.group.getConfig();        	
        	ctx.json(config);
        });
        
        app.get("/dataset/raw/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	OuterDataset outerDataset = this.group.getOuterDataset();
        	ctx.json(outerDataset);
        });
        
        app.get("/feature/histogram/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	String feature = ctx.queryParam("feature");
        	String groupCount = ctx.queryParam("groupCount");
        	
        	JobConfig config = new JobConfig();
        	config.groupCount.put(feature, Integer.parseInt(groupCount));
        	
        	// TODO: refactor this
    		IEnvironment env = new MiningEnvironment(this.group.getOuterDataset());
    		this.env = env;
			env.init(this.unnContext, config);
        });
	}
}
