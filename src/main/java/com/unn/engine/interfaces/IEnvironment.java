package com.unn.engine.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.mining.JobConfig;
import com.unn.engine.metadata.UnitReport;
import com.unn.engine.mining.StatsWalker;
import com.unn.engine.session.Context;

public interface IEnvironment {
	
	ArrayList<IOperator> getInputs(String spaceId);
	
	Integer mapInput(String inputString, String version);
	
	Double predict(String spaceId, HashMap<IOperator, Integer> values);
	
	void init(Context context, JobConfig config);
	
	StatsWalker mine() throws Exception;

	UnitReport getUnitReport();

	StatsWalker getStatsWalker();

	JobConfig getConfig();
}
