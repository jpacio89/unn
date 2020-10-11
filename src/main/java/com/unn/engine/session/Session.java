package com.unn.engine.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.MiningScope;
import com.unn.engine.mining.MiningReport;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.OuterDatasetLoader;
import com.unn.engine.mining.Artifact;
import com.unn.engine.mining.Model;
import com.unn.engine.mining.StatsWalker;
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
	
	public void act(Action _action) {
		Action action = null;
		
		if (_action instanceof LoadDatasetAction) {
			LoadDatasetAction __action = (LoadDatasetAction) _action;
			__action.setContext(this.context);
			__action.setSession(this);
		} else if (_action instanceof SaveModelAction) {
			// Nothing to set
		} else if (_action instanceof MineAction) {
			MineAction __action = (MineAction) _action;
			__action.setConf(this.getMineConfig());
			__action.setSession(this);
		} else if (_action instanceof PublishAction) {
			PublishAction __action = (PublishAction) _action;
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
			StatsWalker stats = env.getStatsWalker();
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
