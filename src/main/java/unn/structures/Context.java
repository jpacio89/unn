package unn.structures;

import java.io.Serializable;
import java.util.HashMap;

import plugins.openml.JobConfig;
import unn.mining.MiningStatusObservable;

public class Context implements Serializable {
	private static final long serialVersionUID = 329875276429497910L;
	private HashMap<String, MiningStatusObservable> statusObservables;
	
	public Context() {
		this.statusObservables = new HashMap<String, MiningStatusObservable>();
	}

	public MiningStatusObservable getStatusObservable(String statusSessionId) {
		return statusObservables.get(statusSessionId);
	}

	public void setStatusObservable(String statusSessionId, MiningStatusObservable statusObservable) {
		this.statusObservables.put(statusSessionId, statusObservable);
	}

	public void registerJobConfig(JobConfig newConfig) {
		setStatusObservable(newConfig.jobSessionId, new MiningStatusObservable());
	}
	
}
