package unn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import plugins.openml.JobConfig;
import plugins.openml.UnitReport;

public interface IEnvironment {
	
	ArrayList<IOperator> getInputs(String spaceId);
	
	Integer mapInput(String inputString, String version);
	
	Double predict(String spaceId, HashMap<IOperator, Integer> values);
	
	void init(JobConfig config);
	
	void mine() throws Exception;

	UnitReport getUnitReport();
}
