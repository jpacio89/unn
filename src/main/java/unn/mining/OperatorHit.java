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
}
