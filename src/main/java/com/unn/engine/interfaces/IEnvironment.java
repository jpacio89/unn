package com.unn.engine.interfaces;

import java.util.ArrayList;
import com.unn.engine.metadata.ValueMapper;
import com.unn.engine.mining.models.ScopeConfig;
import com.unn.engine.session.Session;

public interface IEnvironment {
	ArrayList<IFeature> getInputs(String spaceId);
	void mine(Session session) throws Exception;
	ScopeConfig getConfig();
}
