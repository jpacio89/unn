package com.unn.engine.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.mining.PerformanceAnalyzer;

public interface IEnvironment {
	ArrayList<IFeature> getInputs(String spaceId);
	
	Double predict(HashMap<IFeature, Integer> values);
	
	PerformanceAnalyzer mine() throws Exception;

	ValueMapper getMapper();

	PerformanceAnalyzer getStatisticsAnalyzer();

	ScopeConfig getConfig();
}
