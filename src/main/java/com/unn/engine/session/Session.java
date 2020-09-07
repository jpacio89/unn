package com.unn.engine.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import com.unn.engine.interfaces.IEnvironment;
import com.unn.engine.interfaces.IOperator;
import com.unn.engine.morphing.MorpherOld;
import com.unn.engine.mining.MiningStatus;
import com.unn.engine.mining.JobConfig;
import com.unn.engine.mining.MiningEnvironment;
import com.unn.engine.mining.MiningReport;
import com.unn.engine.metadata.UnitReport;
import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;
import com.unn.engine.dataset.OuterDatasetLoader;
import com.unn.engine.mining.Artifact;
import com.unn.engine.mining.Model;
import com.unn.engine.mining.StatsWalker;
import com.unn.engine.session.actions.Action;
import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.LoadDatasetAction;
import com.unn.engine.session.actions.MineAction;
import com.unn.engine.session.actions.MorphAction;
import com.unn.engine.session.actions.PredictAction;
import com.unn.engine.session.actions.QueryAction;
import com.unn.engine.session.actions.SaveModelAction;
import com.unn.engine.session.actors.Actor;
import com.unn.engine.session.actors.LoadActor;
import com.unn.engine.session.actors.MineActor;
import com.unn.engine.session.actors.MorphActor;
import com.unn.engine.session.actors.PredictActor;
import com.unn.engine.session.actors.QueryActor;
import com.unn.engine.session.actors.PersistenceActor;

public class Session implements Serializable {
	private static final long serialVersionUID = -4066182105363905590L;
	private String sessionName;
	private Context context;
	private OuterDataset outerDataset;
	private HashMap<String, MiningEnvironment> envs;
	IEnvironment env;
	JobConfig mineConfig;
	
	public Session(String sessionName, Context context) {
		this.sessionName = sessionName;
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
		
		if (_action instanceof LoadDatasetAction) {
			LoadDatasetAction action = (LoadDatasetAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof SaveModelAction) {
			SaveModelAction action = (SaveModelAction) _action;
			actor = new PersistenceActor(action);
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
		
		ActionResult result = actor.write();
		return result;
	}
	
	public OuterDataset getOuterDataset() {
		return outerDataset;
	}
	
	public Context getContext() {
		return this.context;
	}
	
	public IEnvironment getEnv() {
		return env;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	// TODO: refactor this
	public void setEnv(IEnvironment env) {
		this.env = env;
	}

	// TODO: refactor this
	public JobConfig getMineConfig() {
		return mineConfig;
	}

	// TODO: refactor this
	public void setMineConfig(JobConfig mineConfig) {
		this.mineConfig = mineConfig;
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
	public HashMap<IOperator, Integer> morph(HashMap<IOperator, Integer> inputs, String classValueFrom, String classValueTo) {
		/*MiningEnvironment seedEnv = new MiningEnvironment(this.datasetId);
		seedEnv.init(config);

		UnitReport units = seedEnv.getUnitReport();
		
		OuterValueType vType = units.getValues(config.targetFeature);*/
		MiningEnvironment from = this.envs.get(classValueFrom);
		MiningEnvironment to = this.envs.get(classValueTo);
		
		UnitReport units = from.getUnitReport();
		//UnitReport unitsTo = to.getUnitReport();
		
		//OuterValueType typeFrom = unitsFrom.getValues("\"type\"");
		//OuterValueType typeTo = unitsTo.getValues("\"type\"");
		
		MorpherOld morpher = new MorpherOld();
		morpher.init(from.getModel(), units, to.getModel());
		
		HashMap<IOperator, Integer> ret = morpher.morph(inputs);
		return ret;
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

	public ArrayList<String> getFeatures() {
		return this.env.getUnitReport().getFeatures();
	}
	
}
