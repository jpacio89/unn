package com.unn.engine.v2.tests;

public class TestOperationsSinusoide 
{
	/*public final int DELAYED_SINUSOIDE_COUNT = 5;
	public final int DELAYED_SINUSOIDE_DEGREE_STEP = 5;
	
	
	private Integer sinusoide(Integer deg) {
		double radians = Math.toRadians(deg);
		return (int) (Math.sin(radians) * (Definitions.STIMULI_MAX_VALUE - 2));
	}
	
	private void getTemporalSinusoides(HashMap<Integer, Integer> cachedRewards, Dataset dataset, ArrayList<IOperator> sinusoides, int time) {
		int n = 0;
		for (int i = time - DELAYED_SINUSOIDE_COUNT * DELAYED_SINUSOIDE_DEGREE_STEP; i < time; i += DELAYED_SINUSOIDE_DEGREE_STEP) {
			IOperator sinusoide = sinusoides.get(n);
			int val = sinusoide(i);
			dataset.add(new VTR(sinusoide, val, time, cachedRewards.get(time)));
			n++;
		}
	}
	
	private Dataset getDatasetSinusoide (IOperator sinusoide, ArrayList<IOperator> delayedSinusoides) {
		int sampleCount = 10000;
		Dataset dataset = new Dataset();		
		HashMap<Integer, Integer> cachedRewards = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < sampleCount; ++i) {
			int sinVal = sinusoide(i);
			int rewardVal = Definitions.STIMULI_MIN_VALUE;
			int v360 = i % 360;
			
			if ((v360 >= 0 && v360 < 90) || (v360 >= 180 && v360 < 270)) {
				rewardVal = Definitions.STIMULI_MAX_VALUE;
			}
			
			cachedRewards.put(i, rewardVal);
			
			dataset.add(new VTR(sinusoide, sinVal, i, rewardVal));
			getTemporalSinusoides(cachedRewards, dataset, delayedSinusoides, i);
		}
		
		return dataset;
	}
	
	@Test
	public void testSinusoide () throws Exception
	{
		Definitions.STIMULI_MIN_VALUE = -10;
		Definitions.STIMULI_MAX_VALUE = 10;
		
		RAW sinusoide = new RAW ();

		sinusoide.setDescriptor(new OperatorDescriptor (".", "sin", 0));
		
		ArrayList<IOperator> delayedSinusoides = new ArrayList<IOperator>();
		
		for (int i = 0; i < DELAYED_SINUSOIDE_COUNT; ++i) {
			RAW sinDelayed = new RAW ();
			sinDelayed.setDescriptor(new OperatorDescriptor (".", "sin[t-" + (DELAYED_SINUSOIDE_COUNT-i) + "]", 1+i));
			delayedSinusoides.add(sinDelayed);
		}
		
		Dataset dataset = getDatasetSinusoide (sinusoide, delayedSinusoides);
		
		Miner miner = new Miner();
		
		ArrayList<IOperator> args = new ArrayList<IOperator>();
		args.add(sinusoide);
		args.addAll(delayedSinusoides);
		
		miner.init(args);
		
		ArrayList<Triplet> report = miner.mine(dataset, 3);
		
		for (int i = 0; i < 360; ++i) {
			predict(dataset, report, sinusoide, delayedSinusoides, i);
		}		
	}

	
	private void predict(
			Dataset dataset,
			ArrayList<Triplet> report, 
			IOperator sinusoide, 
			ArrayList<IOperator> delayedSinusoides,
			int time
		) {
		double percSum = 0;
		int reportRows = 0;
		
		for (Triplet pair : report) {
			ArrayList<Pair<IOperator,Integer>> parcels = (ArrayList<Pair<IOperator, Integer>>) pair.first();
			Integer percentage = (Integer) pair.second();
			
			boolean hit = true;
			
			for (Pair<IOperator, Integer> parcel : parcels) {
				IOperator thd = parcel.first();
				Integer parcelOutcome = parcel.second() == 0 ? Definitions.STIMULI_MIN_VALUE : Definitions.STIMULI_MAX_VALUE;
				
				thd.recycle();
				
				sinusoide.define(dataset.getValueByTime(sinusoide, time));
				
				for(IOperator delayedSinusoide : delayedSinusoides) {
					delayedSinusoide.define(dataset.getValueByTime(delayedSinusoide, time));
				}
				
				try {
					thd.operate();
					int thdOutcome = thd.value();
					
					if (parcelOutcome.intValue() != thdOutcome) {
						hit = false;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}	
			}
			
			if (hit) {
				percSum += percentage;
				reportRows++;
			}
		}
		
		percSum /= reportRows;
		
		System.out.println("Outcome(" + time + ") = " + percSum);
	}
	*/
	

}
