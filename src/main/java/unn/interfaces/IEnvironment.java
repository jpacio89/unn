package unn.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import plugins.openml.JobConfig;
import plugins.openml.UnitReport;
import unn.mining.StatsWalker;

public interface IEnvironment {
	
	ArrayList<IOperator> getInputs(String spaceId);
	
	Integer mapInput(String inputString, String version);
	
	Double predict(String spaceId, HashMap<IOperator, Integer> values);
	
	void init(JobConfig config);
	
	StatsWalker mine() throws Exception;

	UnitReport getUnitReport();

	StatsWalker getStatsWalker();
}
