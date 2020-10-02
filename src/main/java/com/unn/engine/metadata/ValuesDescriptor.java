package com.unn.engine.metadata;

import com.unn.engine.interfaces.IOperator;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 3612068706645305751L;
	public abstract ArrayList<Integer> getAllInnerValues();
	public abstract ArrayList<String> getGroups();
	public abstract IOperator getClassByGroup(String group);
}
