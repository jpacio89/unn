package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.json.JavalinJackson;
import io.javalin.plugin.json.JavalinJson;
import plugins.openml.DiscreteSet;
import plugins.openml.EnvironmentGroup;
import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import plugins.openml.MiningReport;
import plugins.openml.OpenML;
import plugins.openml.SimulationConfig;
import unn.IEnvironment;
import unn.IOperator;
import unn.StatsWalker;

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
        	String jobId = ctx.pathParam("jobId");
        	ctx.json(this.env.getUnitReport());
        });
        
        app.post("/dataset/mine/:jobId", ctx -> {
        	String jobId = ctx.pathParam("jobId");
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
        	String jobId = ctx.pathParam("jobId");
        	IEnvironment env = this.env;
        	MiningReport report = group.getReport();
			ctx.json(report);
        });
        
        app.post("/simulate/:jobId", ctx -> {
        	String jobId = ctx.pathParam("jobId");
        	SimulationConfig conf = ctx.bodyAsClass(SimulationConfig.class);

        	Simulation simulation = new Simulation();
        	simulation.init(conf, this.group);
        	simulation.run();
        	SimulationReport report = simulation.getReport();
        	
        	ctx.json(report);
        });
        
        app.get("/dataset/raw/:jobId", ctx -> {
        	String jobId = ctx.pathParam("jobId");
        	OpenML ml = new OpenML();
        	ml.init(this.group.getConfig());
        	ArrayList<HashMap<String, String>> rawDataset = ml.getRawDataset(Integer.parseInt(this.datasetId));
			ctx.json(rawDataset);
        });
        
        // DEPRECATED
        app.get("/predict", ctx -> {
        	String spaceId = ctx.queryParam("spaceId");
        	String version = ctx.queryParam("version");
        	
        	ArrayList<IOperator> inputs = env.getInputs(spaceId);
        	
        	if (inputs == null) {
        		ctx.result("Prediction = ?");
        		return;
        	}
        	
        	HashMap<IOperator, Integer> values = new HashMap<IOperator, Integer>();
        	
        	for (IOperator input : inputs) {
        		String inputQueryParam = ctx.queryParam(input.toString());
        		Integer value = env.mapInput(inputQueryParam, version);
        		values.put(input, value);
        	}
        	
        	if (values.containsKey("action")) {
        		throw new Exception("Result was mistakenly fed into the query.");
        	}
        	
        	Double action = env.predict(spaceId, values);
        	ctx.result(Double.toString(action));
        });
	}
	
	
	public class User {
	    public final int id = 1;
	    public final String name = "Hi!";
	    public final HashMap<String, String> cenas;
	    
	    public User() {
	    	cenas = new HashMap<String, String>();
	    	cenas.put("Alpha", "Beta");
	    }
	    // constructors
	}
}
