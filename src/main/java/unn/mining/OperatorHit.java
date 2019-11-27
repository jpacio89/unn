package unn.mining;

import unn.interfaces.IOperator;

public class OperatorHit {
	public IOperator operator;
	public int hit;
	
	public OperatorHit(IOperator op, int hit) {
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
		OperatorHit other = (OperatorHit) obj;
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
