package unn.morphing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import unn.interfaces.IOperator;
import unn.mining.Model;
import unn.structures.Config;
import utils.RandomManager;

public class Morpher {
	Model modelFrom;
	Model modelTo;

	public Morpher() {
		
	}
	
	public void init(Model modelFrom, Model modelTo) {
		this.modelFrom = modelFrom;
		this.modelTo = modelTo;
	}
	
	public HashMap<IOperator, Integer> morphOnce(HashMap<IOperator, Integer> inputs, Integer totalVariation, Integer target) {
		Double relaxationFactor = 0.1;
		
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		operators.addAll(inputs.keySet());
		
		Collections.shuffle(operators);
		
		HashMap<IOperator, Integer> newInput = new HashMap<IOperator, Integer>();
		Integer missingVariation = totalVariation;
		int n = 0;
		
		
		while (missingVariation > 0) {
			// TODO: work with steps instead of continuous values
			int gradient = RandomManager.rand(-missingVariation, missingVariation);
			
			IOperator op = operators.get(n);
			Integer val = inputs.get(op);
			Integer newVal = val + gradient;
			
			if (newVal < Config.STIMULI_MIN_VALUE) {
				newVal = Config.STIMULI_MIN_VALUE;
			}
			
			if (newVal > Config.STIMULI_MAX_VALUE) {
				newVal = Config.STIMULI_MAX_VALUE;
			}
			
			missingVariation -= Math.abs(gradient);
			newInput.put(op, newVal);
			n = (n + 1) % operators.size();
		}
		
		for (IOperator op : operators) {
			if (!newInput.containsKey(op)) {
				newInput.put(op, inputs.get(op));
			}
		}
		
		Double outcome = modelFrom.predict(newInput, null, null);
		
		if (Math.abs(outcome - target) < relaxationFactor * Config.STIMULI_RANGE) {
			return newInput;
		}
		
		return null;
	}
}
