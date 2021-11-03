package com.unn.engine.functions;

import java.io.Serializable;

import com.unn.engine.interfaces.IFeature;

public class SimpleFeature implements IFeature, Serializable {
	private static final long serialVersionUID = 9162259575884363749L;
	String name;

	@Override
	public String getName() {
		return this.name;
	}

	public void setName (String _name) {
		this.name = _name;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.name.equals(
			((SimpleFeature) obj).name);
	}
}