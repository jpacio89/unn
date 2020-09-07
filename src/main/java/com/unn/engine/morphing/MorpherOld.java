package com.unn.engine.morphing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.unn.engine.metadata.UnitReport;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.mining.Model;
import com.unn.engine.Config;
import com.unn.engine.utils.RandomManager;

public class MorpherOld {
	UnitReport units;
	Model modelFrom;
	Model modelTo;

	public MorpherOld() {
		
	}
	
	public void init(Model modelFrom, UnitReport units, Model modelTo) {
		this.modelFrom = modelFrom;
		this.modelTo = modelTo;
		this.units = units;
	}
	
	public HashMap<IOperator, Integer> morph(HashMap<IOperator, Integer> inputs) {
		IOperator[] allArguments = this.modelFrom.getDataset().getAllLeaves();
		
		/*HashMap<IOperator, Integer> inputsObjs = new HashMap<IOperator, Integer>();
		
		for (IOperator op : allArguments) {
			if (inputs.containsKey(op.toString())) {
				inputsObjs.put(op, inputs.get(op.toString()));
			}
		}*/
		
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
		
		while (missingVariation > 0) {
			if (n >= operators.size()) {
				n = operators.size() - 1;
				System.out.println("|Morpher| Overflow!");
			}
			
			IOperator op = operators.get(n);
			Integer val = inputs.get(op);
			
			ArrayList<Integer> allPotentialValues = this.units.getValues(operators.get(n).toString()).getAllInnerValues();
			Integer pivot = allPotentialValues.indexOf(val);
			
			int indexGuess = RandomManager.rand(
				Math.max(0, pivot - totalVariation), 
				Math.min(pivot + totalVariation, allPotentialValues.size() - 1)
			);
			
			Integer newVal = allPotentialValues.get(indexGuess);
			
			missingVariation -= Math.abs(pivot - indexGuess);
			newInput.put(op, newVal);
			n = (n + 1); // % operators.size();
		}
		
		for (IOperator op : operators) {
			if (!newInput.containsKey(op)) {
				newInput.put(op, inputs.get(op));
			}
		}
		
		Double outcome = modelTo.predict(newInput, null, null);
		
		if (Math.abs(outcome - target) < relaxationFactor * Config.STIMULI_RANGE) {
			return newInput;
		}
		
		return null;
	}
}
