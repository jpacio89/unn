package unn.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import unn.interfaces.IOperator;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.structures.Config;
import unn.structures.VTR;

public class DatasetParser {

	public static Dataset parseFromFile(String filePath) {
		ArrayList<IOperator> args = null;
		IOperator[] localArgs = null;
		
		Dataset dataset = new Dataset();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
		    String line;
		    int n = -1;
		    
		    while ((line = br.readLine()) != null) { 	
		    	String[] cols = line.split(",");
		    	n++;
		    	
		    	if (n == 0) {
		    		args = new ArrayList<IOperator>();
		    		localArgs = new IOperator[cols.length];

		    		dataset.setTrainingLeaves(args);
		    		// dataset.setAllLeaves(localArgs);

		    		int k = -1;

		    		for (String bopName : cols) {
		    			k++;
		    			if (k < 2) {
		    				continue;
		    			}
		    			RAW bop = new RAW ();
			    		bop.setDescriptor(new OperatorDescriptor (".", bopName, k));
			    		localArgs[k] = bop;
			    		if (!bopName.toString().equals("action")) {
			    			args.add(bop);
			    		}
		    		}
				} else {
					int k = -1;
					ArrayList<VTR> inputs = new ArrayList<VTR>();
					long timeLong = Long.parseLong(cols[1]) / 1000;
					int time = (int) timeLong;
	    			boolean isReward = false;
	    			
		    		for (String bopValStr : cols) {
		    			k++;
		    			if (k < 2) {
		    				continue;
		    			}
		    			
		    			if ("?".equals(bopValStr)) {
		    				break;
		    			}
		    			
		    			// TODO: re-check this
		    			if ("NEUTRAL".equals(bopValStr)) {
		    				break;
		    			}
		    			
		    			int mappedVal = 0;
		    			
		    			switch(bopValStr) {
		    			case "BUY":
		    				mappedVal = Config.STIMULI_MAX_VALUE;
		    				isReward = true;
		    				break;
		    			case "NEUTRAL":
		    				mappedVal = Config.STIMULI_NULL_VALUE;
		    				isReward = true;
		    				break;
		    			case "SELL":
		    				mappedVal = Config.STIMULI_MIN_VALUE;
		    				isReward = true;
		    				break;
	    				default:
	    					mappedVal = mapPrice(Double.parseDouble(bopValStr));
	    					break;
		    			}
		    			
	    				inputs.add(new VTR(localArgs[k], mappedVal, time, mappedVal));

		    			if (isReward) {
		    				for (VTR vtr : inputs) {
		    					vtr.setReward(mappedVal);
		    				}
		    			}
		    		}
		    		
		    		if (isReward && inputs.size() == cols.length - 2) {
		    			for (VTR input : inputs) {
		    				dataset.add(input);
		    			}
		    		}
				}
		    }
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataset;
	}
	
	public static Integer mapPrice(double price) {
		double absPrice = Math.abs(price);
		int absInput = 0;
		
		if (absPrice < 0.5) {
			absInput = 0;
		} else if (absPrice < 1.5) {
			absInput = 1;
		} else if (absPrice < 3.0) {
			absInput = 2;
		} else if (absPrice < 5.0) {
			absInput = 3;
		} else if (absPrice < 8.0) {
			absInput = 4;
		} else if (absPrice < 13.0) {
			absInput = 5;
		} else if (absPrice < 18.0) {
			absInput = 6;
		} else if (absPrice < 25.0) {
			absInput = 7;
		} else if (absPrice < 40.0) {
			absInput = 8;
		} else {
			absInput = 9;
		}
		
		if (price < 0) {
			absInput = -absInput;
		}
		
		// this.histogram[absInput + Definitions.STIMULI_MAX_VALUE]++;
		
		return absInput;
	}
}
