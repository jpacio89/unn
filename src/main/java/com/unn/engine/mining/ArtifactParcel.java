package com.unn.engine.mining;

import java.io.Serializable;

import com.unn.engine.interfaces.IOperator;

public class ArtifactParcel implements Serializable {
	private static final long serialVersionUID = 6076337110545632229L;
	public IOperator operator;
	public int hit;
	
	public ArtifactParcel(IOperator op, int hit) {
		this.operator = op;
		this.hit = hit;
	}

	@Override
	public String toString() {
		return "OperatorHit [" + operator + " = " + hit + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hit;
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArtifactParcel other = (ArtifactParcel) obj;
		if (hit != other.hit)
			return false;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		return true;
	}
	
	
}
