package server;

import java.util.ArrayList;
import java.util.HashMap;

import io.javalin.Javalin;
import unn.IEnvironment;
import unn.IOperator;

public class RESTApi extends Thread {
	IEnvironment env;
	
	public RESTApi(IEnvironment env) {
		this.env = env;
	}
	
	public void run() {
		Javalin app = Javalin.create().start(7000);
        app.get("/", ctx -> ctx.result("UNN server running."));
        
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
	
	
}
