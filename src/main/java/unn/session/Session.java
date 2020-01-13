package unn.session;

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

public class Session {

	
	public Session() {
		
	}
	
	public ActionResult act(Action _action) {
		Actor actor = null;
		
		if (_action instanceof LoadAction) {
			LoadAction action = (LoadAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof SaveAction) {
			SaveAction action = (SaveAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof QueryAction) {
			QueryAction action = (QueryAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof MineAction) {
			MineAction action = (MineAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof PredictAction) {
			PredictAction action = (PredictAction) _action;
			actor = new LoadActor(action);
		} else if (_action instanceof MorphAction) {
			MorphAction action = (MorphAction) _action;
			actor = new LoadActor(action);
		}
		
		ActionResult result = actor.run();
		return result;
	}
}
