package unn.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import plugins.openml.MiningReport;
import plugins.openml.UnitReport;
import unn.dataset.DatasetLocator;
import unn.dataset.OuterDataset;
import unn.dataset.OuterDatasetLoader;
import unn.interfaces.IEnvironment;
import unn.interfaces.IOperator;
import unn.mining.Artifact;
import unn.mining.Model;
import unn.mining.StatsWalker;
import unn.morphing.MorpherOld;
import unn.session.actions.Action;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.MineAction;
import unn.session.actions.MorphAction;
import unn.session.actions.PredictAction;
import unn.session.actions.QueryAction;
import unn.session.actions.SaveModelAction;
import unn.session.actors.Actor;
import unn.session.actors.LoadActor;
import unn.session.actors.MineActor;
import unn.session.actors.MorphActor;
import unn.session.actors.PredictActor;
import unn.session.actors.QueryActor;
import unn.session.actors.PersistenceActor;
import unn.structures.Context;
import unn.structures.MiningStatus;

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
