package unn.morphing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import plugins.openml.OuterValueType;
import plugins.openml.UnitReport;
import unn.interfaces.IOperator;
import unn.mining.Model;
import unn.structures.Config;
import utils.RandomManager;

public class Morpher {
	OuterValueType unitsFrom;
	OuterValueType unitsTo;
	Model modelFrom;
	Model modelTo;

	public Morpher() {
		
	}
	
	public void init(Model modelFrom, OuterValueType unitsFrom, Model modelTo, OuterValueType unitsTo) {
		this.modelFrom = modelFrom;
		this.modelTo = modelTo;
		this.unitsFrom = unitsFrom;
		this.unitsTo = unitsTo;
	}
	
	public HashMap<IOperator, Integer> morph(HashMap<IOperator, Integer> inputs) {
		Integer totalVariation = 1;
		
		// TODO: make this better
		for (int i = 0; i < 10000; ++i) {
			HashMap<IOperator, Integer> ret = morphOnce(inputs, totalVariation, Config.STIMULI_MAX_VALUE);
			if (ret != null) {
				return ret;
			}
			if (i % 1000 == 0) {
				totalVariation++;
			}
		}
		
		return null;
	}
	
	public HashMap<IOperator, Integer> morphOnce(HashMap<IOperator, Integer> inputs, Integer totalVariation, Integer target) {
		Double relaxationFactor = 0.1;
		
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		operators.addAll(inputs.keySet());
		
		Collections.shuffle(operators);
		
		HashMap<IOperator, Integer> newInput = new HashMap<IOperator, Integer>();
		Integer missingVariation = totalVariation;
		int n = 0;
		
		ArrayList<Integer> allPotentialValues = this.unitsFrom.getAllInnerValues();
		
		while (missingVariation > 0) {
			int indexGuess = RandomManager.rand(0, allPotentialValues.size());
			int upDown = RandomManager.rand(0, 1);
			int gradient = upDown == 0 ? -allPotentialValues.get(indexGuess) : allPotentialValues.get(indexGuess);
			
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
