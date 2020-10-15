package com.unn.engine.metadata;

import com.unn.engine.interfaces.IFunctor;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class ValuesDescriptor implements Serializable {
	public abstract ArrayList<String> getGroups(String suffix);
	public abstract IFunctor getFunctorByGroup(String group);
	public abstract ArrayList<String> getGroupByOuterValue(String outerFeatureValue, String suffix);
}
