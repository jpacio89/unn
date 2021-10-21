package com.unn.engine.metadata;

import com.unn.engine.interfaces.IFunctor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public abstract class ValuesDescriptor implements Serializable {
	protected String suffix;

	public ValuesDescriptor() {
		this.suffix = UUID.randomUUID().toString()
			.replace("-", "")
			.substring(0, 10);
	}

	public ValuesDescriptor(String _suffix) {
		this.suffix = _suffix;
	}

	public String getSuffix() {
		return suffix;
	}

	public abstract ArrayList<String> getGroups();
	public abstract IFunctor getFunctorByGroup(String group);
	public abstract ArrayList<String> getGroupByOuterValue(String outerFeatureValue);
}
