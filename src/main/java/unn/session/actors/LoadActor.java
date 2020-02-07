package unn.session.actors;

import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import unn.interfaces.IEnvironment;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;

public class LoadActor extends Actor {
	LoadDatasetAction action;
	
	public LoadActor(LoadDatasetAction action) {
		this.action = action;
	}

	public ActionResult write() {	
    	// TODO: fix hardcoded openml dataset id
    	this.action.getSession().loadOuterDataset(this.action.getLocator());
    	
    	// TODO: remove this
		/*IEnvironment env = new MiningEnvironment(this.group.getOuterDataset());
		this.env = env;
		env.init(action.getContext(), JobConfig.DEFAULT);*/
		return null;
	}

	
}
