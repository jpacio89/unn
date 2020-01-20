package unn.session.actors;

import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import unn.interfaces.IEnvironment;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadAction;

public class LoadActor extends Actor {
	LoadAction action;
	
	public LoadActor(LoadAction action) {
		this.action = action;
	}

	public ActionResult run() {	
    	// TODO: fix hardcoded openml dataset id
    	this.action.getSession().loadOuterDataset(this.action.getLocator());
    	
    	// TODO: remove this
		/*IEnvironment env = new MiningEnvironment(this.group.getOuterDataset());
		this.env = env;
		env.init(action.getContext(), JobConfig.DEFAULT);*/
		return null;
	}

	
}
