package com.unn.engine.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.unn.engine.mining.models.JobConfig;
import com.unn.engine.mining.models.MiningScope;
import com.unn.engine.mining.models.MiningReport;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.OuterDatasetLoader;
import com.unn.engine.mining.models.Artifact;
import com.unn.engine.mining.models.Model;
import com.unn.engine.mining.StatisticsAnalyzer;
import com.unn.engine.session.actions.*;

public class Session implements Serializable {
	private static final long serialVersionUID = -4066182105363905590L;
	private Context context;
	private OuterDataset outerDataset;
	private HashMap<String, MiningScope> scopes;
	JobConfig mineConfig;
	
	public Session(Context context) {
		this.scopes = new HashMap<>();
		this.context = context;
	}
	
	public void loadOuterDataset(DatasetLocator locator) {
		try {
			OuterDatasetLoader loader = new OuterDatasetLoader();
			this.outerDataset = loader.load(locator);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void act(Action action) {
		if (action instanceof LoadDatasetAction) {
			LoadDatasetAction __action = (LoadDatasetAction) action;
			__action.setContext(this.context);
			__action.setSession(this);
		} else if (action instanceof SaveModelAction) {
			// Nothing to set
		} else if (action instanceof MineAction) {
			MineAction __action = (MineAction) action;
			__action.setConf(this.getMineConfig());
			__action.setSession(this);
		} else if (action instanceof PublishAction) {
			PublishAction __action = (PublishAction) action;
			__action.setSession(this);
		}
		action.act();
	}
	
	public OuterDataset getOuterDataset() {
		return outerDataset;
	}
	
	public Context getContext() {
		return this.context;
	}

	public JobConfig getMineConfig() {
		return mineConfig;
	}

	public void setMineConfig(JobConfig mineConfig) {
		this.mineConfig = mineConfig;
	}

	// TODO: refactor this
	public HashMap<String, MiningScope> getScopes() {
		return this.scopes;
	}
	
	// TODO: refactor this
	public MiningReport getReport() {
		MiningReport report = new MiningReport();

		for (String value : scopes.keySet()) {
			MiningScope env = scopes.get(value);
			StatisticsAnalyzer stats = env.getStatsWalker();
			Model model = env.getModel();
			report.confusionMatrixes.put(value, stats);
			
			ArrayList<String> artifactSigs = new ArrayList<>();
			
			if (model != null && model.getArtifacts() != null) {
				for (Artifact fact : model.getArtifacts()) {
					artifactSigs.add(fact.toString());
				}
				
				Collections.sort(artifactSigs);				
			}

			report.artifactSignatures.put(value, artifactSigs);
		}

		return report;
	}
}
