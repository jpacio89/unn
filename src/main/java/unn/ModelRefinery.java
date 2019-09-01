package unn;

import java.util.ArrayList;
import java.util.Collections;

import utils.RandomManager;

public class ModelRefinery {
	Model model;
	Miner miner;
	
	public ModelRefinery(Miner miner, Model model) {
		this.model = model;
		this.miner = miner;
	}
	
	public Model refine() {
		ArrayList<Artifact> artifacts = this.model.getArtifacts();
		Dataset dataset = this.model.getDataset();
		
		Model bestModel = null;
		double bestRatio = 0;
		int bestHighs = 0;
		int runCount = 100;
		
		for (int i = 0; i < runCount; ++i) {			
			int rand = RandomManager.rand(1, artifacts.size() - 1);
			
			Collections.shuffle(artifacts);
			
			ArrayList<Artifact> sublist = new ArrayList<Artifact> (artifacts.subList(0, rand));
			
			Model subModel = new Model(dataset, sublist);
			
			miner.gatherStats(subModel);
			
			int[][] matrix = subModel.getStatsWalker().getHitMatrix();
			int lows = matrix[2][0];
			int highs = matrix[2][2];
			
			if (highs == 0) {
				continue;
			}
			
			
			double ratio = 1.0 * highs / (highs + lows);
			
			if (ratio >= bestRatio && highs > bestHighs) {
				bestModel = subModel;
				bestRatio = ratio;
				bestHighs = highs;
				subModel.getStatsWalker().print();
			}
			
			if (i % 10 == 0) {
				System.out.println(String.format("Refining progress: %4.3f", i * 100.0 / runCount));
			}
		}
		
		return bestModel;
	}
}
