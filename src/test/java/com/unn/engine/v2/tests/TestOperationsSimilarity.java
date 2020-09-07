package com.unn.engine.v2.tests;

public class TestOperationsSimilarity 
{
	/*private Integer similarity(Integer x, Integer y) {
		if (x == y + 2) {
			return Definitions.STIMULI_MAX_VALUE;
		}
		return Definitions.STIMULI_MIN_VALUE;
	}
	
	private Dataset getDatasetSimilarity (IOperator lh, IOperator rh) {
		Dataset dataset = new Dataset();
		
		for (int i = 0; i < 10000; ++i) {
			int x = RandomManager.rand (
					Definitions.STIMULI_MIN_VALUE, 
					Definitions.STIMULI_MAX_VALUE
			);
			int y = RandomManager.rand (
				Definitions.STIMULI_MIN_VALUE, 
				Definitions.STIMULI_MAX_VALUE
			);
			int returns = similarity(x, y);
			
			dataset.add(new VTR(lh, x, i, returns));
			dataset.add(new VTR(rh, y, i, returns));
		}
		
		return dataset;
	}
	
	@Test
	public void testSimilarity () throws Exception
	{
		PrintStream fileOut = new PrintStream("./out.log");
		// System.setOut(fileOut);
		
		Definitions.STIMULI_MIN_VALUE = -10;
		Definitions.STIMULI_MAX_VALUE = 10;
		
		RAW x = new RAW ();
		RAW y = new RAW ();
		RAW z = new RAW ();

		x.setDescriptor(new OperatorDescriptor (".", "x", 0));
		y.setDescriptor(new OperatorDescriptor (".", "y", 1));
		z.setDescriptor(new OperatorDescriptor (".", "z", 2));
		
		Dataset dataset = getDatasetSimilarity (x, y);
		
		Miner miner = new Miner();
		
		ArrayList<IOperator> args = new ArrayList<IOperator>();
		args.add(x);
		args.add(y);
		
		miner.init(args);
		
		CoinMiner cMiner = new CoinMiner(miner);
		//ArrayList<Pair> report = miner.mine(dataset, 7);	
		ArrayList<Triplet> report = cMiner.mine(dataset, 3);

		predict(report, x, y, 2, 2);
		predict(report, x, y, 7, 5);
		predict(report, x, y, 4, 0);
		predict(report, x, y, 7, 9);
		predict(report, x, y, -3, -5);
		predict(report, x, y, 4, 2);
		predict(report, x, y, 0, 6);
	}
	
	private void predict(ArrayList<Triplet> report, IOperator xOp, IOperator yOp, int xVal, int yVal) {
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
				
				xOp.define(xVal);
				yOp.define(yVal);
				
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
		
		System.out.println("Outcome = " + percSum);
	}*/
	
	

}
