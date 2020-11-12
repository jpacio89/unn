package com.unn.engine.mining;

import com.unn.engine.Config;
import com.unn.engine.dataset.InnerDataset;
import com.unn.engine.mining.models.Artifact;
import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.models.Model;
import com.unn.engine.utils.Pair;
import com.unn.engine.utils.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class RefineryV3 {
	private Model model;
	private Miner miner;

	public RefineryV3(Miner miner, Model model) {
		this.model = model;
		this.miner = miner;
	}

	public Model refine() {
		ArrayList<Pair<Artifact, Integer>> scores = getScores();
		//scores.sort((x, y) -> y.second() - x.second());
		//scores.forEach(score -> System.out.print(score.second() + ", "));
		ArrayList<Artifact> artifacts = scores.stream()
			.filter(score -> score.second() > 0)
			.map(score -> score.first())
			.collect(Collectors.toCollection(ArrayList::new));
		this.model.setArtifacts(artifacts);
		this.miner.gatherStats(this.model);
		return this.model;
	}
	
	public ArrayList<Pair<Artifact, Integer>> getScores() {
		ArrayList<Artifact> artifacts = this.model.getArtifacts();
		ArrayList<Pair<Artifact, Integer>> scores = new ArrayList<>();

		for (int artifactIndex = 0; artifactIndex < artifacts.size(); ++artifactIndex) {
			scores.add(new Pair<>(artifacts.get(artifactIndex), 0));
			score(scores, this.miner.getHighs(), artifactIndex, Config.STIM_MAX);
			score(scores, this.miner.getLows(), artifactIndex, Config.STIM_MIN);
		}

		return scores;
	}

	public void score(ArrayList<Pair<Artifact, Integer>> scores, ArrayList<Integer> times, int artifactIndex, int hitValue) {
		Artifact artifact = this.model.getArtifacts().get(artifactIndex);
		times.stream().forEach(time -> {
			boolean isHit = this.model.isHit(time, artifactIndex);
			Integer value = scores.get(artifactIndex).second();
			if (isHit) {
				scores.get(artifactIndex).second(value + artifact.reward == hitValue ? 1 : -1);
			}
		});
	}


}
