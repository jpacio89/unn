package unn.v2.tests;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.junit.Test;

import unn.interfaces.IOperator;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.operations.THD;
import unn.structures.Config;
import utils.Pair;
import utils.RandomManager;
import utils.Triplet;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestOperationsXOR 
{
	private Integer xor(Integer x, Integer y) {
		if (x.intValue() == y.intValue()) {
			return Config.STIMULI_MIN_VALUE;
		}
		return Config.STIMULI_MAX_VALUE;
	}
	
	private ArrayList<Triplet> getDatasetXOR () {
		int[] possibleValues = { 
			Config.STIMULI_MIN_VALUE, 
			Config.STIMULI_MAX_VALUE 
		};
		
		ArrayList<Triplet> dataset = new ArrayList<Triplet>();
		
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				for (int k = 0; k < 5; ++k) {
					Triplet outcome = new Triplet(possibleValues[i], possibleValues[j], xor(possibleValues[i], possibleValues[j]));
					dataset.add(outcome);
				}
				
			}
		}
		
		return dataset;
	}
	
	private double calculateAcc(ArrayList<Triplet> dataset, THD op) throws Exception {
		int correct = 0;
		op.recycle();
		
		ArrayList<IOperator> params = new ArrayList<IOperator>();
		op.getParameters(params);
		op.getParameters(params);
		for (Triplet item : dataset) {
			op.recycle();
			for(IOperator param : params) {
				param.define(param.toString().equals(".x") ? (Integer) item.first() : (Integer) item.second());
			}
			op.operate(null);
			int outcome = op.value();
			
			if (outcome == (Integer) item.third()) {
				correct++;
			}
		}
		
		double acc = Math.floor(correct * 100.0 / dataset.size());
		
		return acc;
	}
	
	@Test
	public void testXor () throws Exception
	{		
		ArrayList<Triplet> dataset = getDatasetXOR ();
		PriorityQueue<Pair> outcomes = new PriorityQueue<Pair>(new AccComparator());
		
		RAW x = new RAW ();
		RAW y = new RAW ();

		x.setDescriptor(new OperatorDescriptor (".", ".x", 0));
		y.setDescriptor(new OperatorDescriptor (".", ".y", 1));
		
		OperationPool pool = new OperationPool(x, y);
		
		for(int i = 0; i < 1000; ++i) {
			THD t = pool.next();
			
			//x.define(Definitions.STIMULI_MIN_VALUE);
			//y.define(Definitions.STIMULI_MAX_VALUE);
			//t.operate();
			//System.out.println(t.value());
			
			double acc = calculateAcc(dataset, t);
			outcomes.add(new Pair(t, acc));
		}
		
		Pair best = outcomes.peek();
		THD bestOp = (THD) best.first();
		Double bestAcc = (Double) best.second();
		
		System.out.println(bestOp.toString() + " = " + bestAcc);
		
		bestOp.recycle();
		x.define(Config.STIMULI_MIN_VALUE);
		y.define(Config.STIMULI_MIN_VALUE);
		bestOp.operate(null);
		System.out.println("FF = " + bestOp.value());
		
		bestOp.recycle();
		x.define(Config.STIMULI_MIN_VALUE);
		y.define(Config.STIMULI_MAX_VALUE);
		bestOp.operate(null);
		System.out.println("FT = " + bestOp.value());
		
		bestOp.recycle();
		x.define(Config.STIMULI_MAX_VALUE);
		y.define(Config.STIMULI_MIN_VALUE);
		bestOp.operate(null);
		System.out.println("TF = " + bestOp.value());
		
		bestOp.recycle();
		x.define(Config.STIMULI_MAX_VALUE);
		y.define(Config.STIMULI_MAX_VALUE);
		bestOp.operate(null);
		System.out.println("TT = " + bestOp.value());	
	}
	
	class OperationPool {
		ArrayList<IOperator> pool;
		
		public OperationPool(RAW x, RAW y) {
			pool = new ArrayList<IOperator>();
			
			//THD z = new THD(new THD(x, new THD(x,y)), new THD(y, new THD(x,y)));
			//THD xor = new THD(z,z);
			//pool.add((IOperator) xor);
			
			pool.add((IOperator) x);
			pool.add((IOperator) y);
			//pool.add(new RAW(Definitions.STIMULI_MIN_VALUE));
			//pool.add(new RAW(Definitions.STIMULI_MAX_VALUE));
			//pool.add(new RAW(Definitions.STIMULI_NULL_VALUE));
			
			
			//
			// { [ A NOR ( A NOR B ) ] NOR [ B NOR ( A NOR B ) ] } NOR { [ A NOR ( A NOR B ) ] NOR [ B NOR ( A NOR B ) ] }	 
		}
		
		public THD next() {
			int guess1 = RandomManager.rand(0, pool.size() - 1);
			int guess2 = RandomManager.rand(0, pool.size() - 1);
			THD next = new THD(pool.get(guess1), pool.get(guess2));
			pool.add(next);
			return next;
			//return (THD) pool.get(0);
		}
	}
	
	class AccComparator implements Comparator<Pair>{ 
		public int compare(Pair acc1, Pair acc2) { 
            if ((Double) acc1.second() < (Double) acc2.second()) {
            	return 1;
            } else if ((Double) acc1.second() > (Double) acc2.second()) {
            	return -1;
            }
            if (acc1.first().toString().length() < acc2.first().toString().length()) {
            	return -1;
            } else if (acc1.first().toString().length() > acc2.first().toString().length()) {
            	return 1;
            }
            return 0; 
        }
    } 
}
