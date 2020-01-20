package unn.session.actions;

import plugins.openml.JobConfig;

public class MineAction extends Action {
	JobConfig conf;
	
	public MineAction(JobConfig _conf) {
		this.conf = _conf;
	}

	public JobConfig getConf() {
		return conf;
	}

	public void setConf(JobConfig conf) {
		this.conf = conf;
	}
	
	
}
