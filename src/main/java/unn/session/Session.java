package unn.session;

import java.util.HashMap;

import plugins.openml.MiningEnvironment;
import unn.dataset.OuterDataset;
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

public class Session {
	private Context context;
	private OuterDataset outerDataset;
	private HashMap<String, MiningEnvironment> envs;
	
	public Session(Context context) {
		this.envs = new HashMap<String, MiningEnvironment>();
		this.context = context;
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
}
