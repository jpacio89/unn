package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class OuterValueType implements Serializable {
	private static final long serialVersionUID = 3612068706645305751L;
	public abstract ArrayList<Integer> getAllInnerValues();
}
