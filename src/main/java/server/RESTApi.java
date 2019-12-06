package server;

import java.util.ArrayList;
import java.util.HashMap;

import io.javalin.Javalin;

import plugins.openml.EnvironmentGroup;
import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import plugins.openml.MiningReport;
import plugins.openml.OpenML;
import plugins.openml.SimulationConfig;
import plugins.openml.UnitReport;
import unn.interfaces.IEnvironment;
import unn.simulation.Simulation;
import unn.simulation.SimulationReport;
import unn.structures.MiningStatus;

public class RESTApi extends Thread {
	EnvironmentGroup group;
	IEnvironment env;
	String datasetId;
	
	public RESTApi() {}
	
	public void run() {
		Javalin app = Javalin.create(config -> {
            config.enableCorsForOrigin("http://localhost:8080/");
        });
		app.start(7000);
        app.get("/", ctx -> ctx.result("UNN server running."));
        
        app.post("/dataset/load/:id", ctx -> {
        	this.datasetId = ctx.pathParam("id");
    		IEnvironment env = new MiningEnvironment(Integer.parseInt(datasetId));
    		this.env = env;
			env.init(JobConfig.DEFAULT);
        });
        
        app.get("/dataset/units/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	ctx.json(this.env.getUnitReport());
        });
        
        app.post("/dataset/mine/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	JobConfig conf = ctx.bodyAsClass(JobConfig.class);
        	this.group = new EnvironmentGroup(Integer.parseInt(datasetId));
        	
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
        	OpenML ml = new OpenML();
        	ml.init(this.group.getConfig(), null);
        	ArrayList<HashMap<String, String>> rawDataset = ml.getRawDataset(Integer.parseInt(this.datasetId));
			ctx.json(rawDataset);
        });
        
        app.get("/feature/histogram/:jobId", ctx -> {
        	// String jobId = ctx.pathParam("jobId");
        	String feature = ctx.queryParam("feature");
        	String groupCount = ctx.queryParam("groupCount");
        	
        	JobConfig config = new JobConfig();
        	config.groupCount.put(feature, Integer.parseInt(groupCount));
        	
    		IEnvironment env = new MiningEnvironment(Integer.parseInt(datasetId));    		
			env.init(config);
    		this.env = env;
        });
	}
}
