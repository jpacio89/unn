package com.unn.engine.interfaces;

import java.util.ArrayList;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.models.ScopeConfig;

public interface IEnvironment {
	ArrayList<IFeature> getInputs(String spaceId);
	void mine() throws Exception;
	ValueMapper getMapper();
	ScopeConfig getConfig();
}
