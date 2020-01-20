package unn.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import plugins.openml.MiningEnvironment;
import plugins.openml.MiningReport;
import plugins.openml.UnitReport;
import unn.dataset.DatasetLocator;
import unn.dataset.OuterDataset;
import unn.dataset.OuterDatasetLoader;
import unn.mining.Artifact;
import unn.mining.Model;
import unn.mining.StatsWalker;
import unn.session.actions.Action;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;
import unn.session.actions.MineAction;
import unn.session.actions.MorphAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveAction;
import unn.session.actors.Actor;
import unn.session.actors.LoadActor;
import unn.session.actors.MineActor;
import unn.session.actors.MorphActor;
import unn.session.actors.PredictActor;
import unn.session.actors.QueryActor;
import unn.session.actors.SaveActor;
import unn.structures.Context;
import unn.structures.MiningStatus;

public class Session {
	private Context context;
	private OuterDataset outerDataset;
	private HashMap<String, MiningEnvironment> envs;
	
	public Session(Context context) {
		this.envs = new HashMap<String, MiningEnvironment>();
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
	
	public ActionResult act(Action _action) {
		Actor actor = null;
		
		if (_action instanceof LoadAction) {
			LoadAction action = (LoadAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof SaveAction) {
			SaveAction action = (SaveAction) _action;
			actor = new SaveActor(action);
		} else if (_action instanceof QueryAction) {
			QueryAction action = (QueryAction) _action;
			actor = new QueryActor(action);
		} else if (_action instanceof MineAction) {
			MineAction action = (MineAction) _action;
			actor = new MineActor(this, action);
		} else if (_action instanceof PredictAction) {
			PredictAction action = (PredictAction) _action;
			actor = new PredictActor(action);
		} else if (_action instanceof MorphAction) {
			MorphAction action = (MorphAction) _action;
			actor = new MorphActor(action);
		}
		
		ActionResult result = actor.run();
		return result;
	}
	
	public OuterDataset getOuterDataset() {
		return outerDataset;
	}
	
	public Context getContext() {
		return this.context;
	}

	// TODO: refactor this
	public HashMap<String, MiningEnvironment> getEnvs() {
		return this.envs;
	}
	
	// TODO: refactor this
	public MiningReport getReport() {
		MiningReport report = new MiningReport();

		for (String value : envs.keySet()) {
			MiningEnvironment env = envs.get(value);
			StatsWalker stats = env.getStatsWalker();
			Model model = env.getModel();
			report.confusionMatrixes.put(value, stats);
			
			ArrayList<String> artifactSigs = new ArrayList<String>();
			
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
	
	// TODO: refactor this
	public HashMap<String, MiningStatus> getMiningStatuses() {
		HashMap<String, MiningStatus> statuses = new HashMap<String, MiningStatus>();
		
		for (Entry<String, MiningEnvironment> entry : this.envs.entrySet()) {
			statuses.put(entry.getKey(), entry.getValue().getMiningStatus());
		}
		
		return statuses;
	}
	
	// TODO: refactor this
	public HashMap<String, UnitReport> getUnitReports() {
		HashMap<String, UnitReport> reports = new HashMap<String, UnitReport>();
		
		for (Entry<String, MiningEnvironment> entry : this.envs.entrySet()) {
			reports.put(entry.getKey(), entry.getValue().getUnitReport());
		}
		
		return reports;
	}
	
}
