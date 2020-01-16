package unn.session.actions;

import unn.dataset.DatasetLocator;

public class LoadAction extends Action {
	DatasetLocator locator;
	
	public LoadAction(DatasetLocator _locator) {
		this.locator = _locator;
	}
}
