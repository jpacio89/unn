package com.unn.engine.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.StatsWalker;
import com.unn.engine.session.Context;

public interface IEnvironment {
	ArrayList<IFunctor> getInputs(String spaceId);
	
	Double predict(HashMap<IFunctor, Integer> values);
	
	void init(Context context, JobConfig config);
	
	StatsWalker mine() throws Exception;

	ValueMapper getMapper();

	StatsWalker getStatsWalker();

	JobConfig getConfig();
}
