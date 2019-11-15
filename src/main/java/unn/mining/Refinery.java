package unn.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import unn.dataset.Dataset;
import unn.structures.Config;
import utils.RandomManager;

public class Refinery {
	private Model model;
	private Miner miner;
	
	public Refinery(Miner miner, Model model) {
		this.model = model;
		this.miner = miner;
	}
	
	public Model refine() {
		ArrayList<Artifact> allArtifacts = new ArrayList<Artifact>();
		allArtifacts.addAll(refineByType(true));
		allArtifacts.addAll(refineByType(false));
		
		Model refinedModel = new Model(model.getDataset(), allArtifacts);
		miner.gatherStats(refinedModel);
		
		return refinedModel;
	}
	
	public ArrayList<Artifact> refineByType(boolean isWheat) {
		ArrayList<Artifact> refinedArtifacts = new ArrayList<Artifact>();
		
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.addAll(model.getArtifacts());
		
		Predicate<Artifact> wheatFilter = artifact -> isWheat ? artifact.reward == Config.STIMULI_MAX_VALUE : artifact.reward == Config.STIMULI_MIN_VALUE;
		artifacts = new ArrayList<Artifact>(artifacts.stream().filter(wheatFilter).collect(Collectors.toList()));
        
		ArrayList<Integer> totalHighs;
		
		if (isWheat) {
			totalHighs = this.miner.getHighs();
		} else {
			totalHighs = this.miner.getLows();
		}
		
		ArrayList<Integer> wheatFound = new ArrayList<Integer>();
		
		Artifact seedArtifact = Collections.max(artifacts, new Comparator<Artifact>() {
		    @Override
		    public int compare(Artifact first, Artifact second) {
		        if (first.wheatTimes.size() > second.wheatTimes.size())
		            return 1;
		        else if (first.wheatTimes.size() < second.wheatTimes.size())
		            return -1;
		        return 0;
		    }
		});
		
		refinedArtifacts.add(seedArtifact);
		wheatFound.addAll(seedArtifact.wheatTimes);
		artifacts.remove(seedArtifact);
		
		while (wheatFound.size() < totalHighs.size() && artifacts.size() > 0) {
			Artifact nextArtifact = Collections.max(artifacts, new Comparator<Artifact>() {
			    @Override
			    public int compare(Artifact first, Artifact second) {
			    	ArrayList<Integer> firstWheats = new ArrayList<Integer>();
			    	firstWheats.addAll(first.wheatTimes);
			    	
			    	ArrayList<Integer> secondWheats = new ArrayList<Integer>();
			    	secondWheats.addAll(second.wheatTimes);
			    	
			    	firstWheats.retainAll(wheatFound);
			    	secondWheats.retainAll(wheatFound);
			    	
			    	int firstDiff = first.wheatTimes.size() - firstWheats.size();
			    	int secondDiff = second.wheatTimes.size() - secondWheats.size();
					
			        if (firstDiff > secondDiff)
			            return 1;
			        else if (firstDiff < secondDiff)
			            return -1;
			        return 0;
			    }
			});
			
			refinedArtifacts.add(nextArtifact);
			wheatFound.addAll(nextArtifact.wheatTimes);
			artifacts.remove(nextArtifact);
		}
		
		return refinedArtifacts;
	}
	
	
	/*public Model refine() {
		ArrayList<Artifact> refinedArtifacts = new ArrayList<Artifact>();
		
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.addAll(model.getArtifacts());
		
		ArrayList<Integer> totalHighs = this.miner.getHighs();
		ArrayList<Integer> wheatFound = new ArrayList<Integer>();
		
		Artifact seedArtifact = Collections.max(artifacts, new Comparator<Artifact>() {
		    @Override
		    public int compare(Artifact first, Artifact second) {
		        if (first.wheatTimes.size() > second.wheatTimes.size())
		            return 1;
		        else if (first.wheatTimes.size() < second.wheatTimes.size())
		            return -1;
		        return 0;
		    }
		});
		
		refinedArtifacts.add(seedArtifact);
		wheatFound.addAll(seedArtifact.wheatTimes);
		artifacts.remove(seedArtifact);
		
		while (wheatFound.size() < totalHighs.size() && artifacts.size() > 0) {
			Artifact nextArtifact = Collections.min(artifacts, new Comparator<Artifact>() {
			    @Override
			    public int compare(Artifact first, Artifact second) {
			    	ArrayList<Integer> firstWheats = new ArrayList<Integer>();
			    	firstWheats.addAll(first.wheatTimes);
			    	
			    	ArrayList<Integer> secondWheats = new ArrayList<Integer>();
			    	secondWheats.addAll(second.wheatTimes);
			    	
			    	firstWheats.retainAll(wheatFound);
			    	secondWheats.retainAll(wheatFound);
					
			        if (firstWheats.size() > secondWheats.size())
			            return 1;
			        else if (firstWheats.size() < secondWheats.size())
			            return -1;
			        return 0;
			    }
			});
			
			refinedArtifacts.add(nextArtifact);
			wheatFound.addAll(nextArtifact.wheatTimes);
			artifacts.remove(nextArtifact);
		}
		
		Model refinedModel = new Model(model.getDataset(), refinedArtifacts);	
		return refinedModel;
	}*/
}
