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
	
	private ArrayList<Artifact> refineByType (boolean isWheat) {
		ArrayList<Integer> timesFound = new ArrayList<Integer>();
		ArrayList<Artifact> refinedArtifacts = new ArrayList<Artifact>();
		
		ArrayList<Artifact> artifacts = this.filterArtifacts(isWheat);
		ArrayList<Integer> allTimes = this.getTimes(isWheat);
		
		Artifact seedArtifact = this.getSeedArtifact(artifacts);
		
		if (seedArtifact == null) {
			return refinedArtifacts;
		}
		
		this.appendArtifact(refinedArtifacts, timesFound, artifacts, seedArtifact);
		
		while (timesFound.size() < allTimes.size() && artifacts.size() > 0) {
			Artifact nextArtifact = this.getNextArtifact(artifacts, timesFound);
			this.appendArtifact(refinedArtifacts, timesFound, artifacts, nextArtifact);
		}
		
		return refinedArtifacts;
	}
	
	private ArrayList<Integer> getTimes(boolean isWheat) {
		if (isWheat) {
			return this.miner.getHighs();
		}
		return this.miner.getLows();
	}
	
	private ArrayList<Artifact> filterArtifacts(boolean isWheat) {
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>(this.model.getArtifacts());
		Predicate<Artifact> wheatFilter = artifact -> {
			return isWheat ? artifact.reward == Config.STIMULI_MAX_VALUE : artifact.reward == Config.STIMULI_MIN_VALUE;
		};
        return new ArrayList<Artifact>(artifacts
        		.stream()
        		.filter(wheatFilter)
        		.collect(Collectors.toList()));
	}
	
	private Artifact getNextArtifact(ArrayList<Artifact> artifacts, ArrayList<Integer> wheatFound) {
		Artifact nextArtifact = Collections.max(artifacts, new Comparator<Artifact>() {
		    @Override
		    public int compare(Artifact first, Artifact second) {
		    	ArrayList<Integer> firstWheats = new ArrayList<Integer>(first.wheatTimes);		    	
		    	ArrayList<Integer> secondWheats = new ArrayList<Integer>(second.wheatTimes);
		    	
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
		
		return nextArtifact;
	}
	
	private Artifact getSeedArtifact(ArrayList<Artifact> artifacts) {
		if (artifacts.size() == 0) {
			return null;
		}
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
		return seedArtifact;
	}
	
	private void appendArtifact(
			ArrayList<Artifact> refinedArtifacts, 
			ArrayList<Integer> wheatFound, 
			ArrayList<Artifact> artifacts, 
			Artifact artifact) {
		refinedArtifacts.add(artifact);
		wheatFound.addAll(artifact.wheatTimes);
		artifacts.remove(artifact);
	}
}
