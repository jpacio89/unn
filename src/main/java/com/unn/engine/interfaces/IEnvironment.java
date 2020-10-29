package com.unn.engine.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.mining.StatisticsAnalyzer;

public interface IEnvironment {
	ArrayList<IFunctor> getInputs(String spaceId);
	
	Double predict(HashMap<IFunctor, Integer> values);
	
	StatisticsAnalyzer mine() throws Exception;

	ValueMapper getMapper();

	StatisticsAnalyzer getStatisticsAnalyzer();

	ScopeConfig getConfig();
}
