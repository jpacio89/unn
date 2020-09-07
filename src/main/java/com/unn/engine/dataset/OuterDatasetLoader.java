package com.unn.engine.dataset;

import com.unn.engine.dataset.datacenter.DatacenterDatasetProvider;
import com.unn.engine.dataset.datacenter.DatacenterLocator;
import com.unn.engine.dataset.filesystem.FilesystemDatasetProvider;
import com.unn.engine.dataset.filesystem.FilesystemLocator;
import com.unn.engine.dataset.openml.OpenMLDatasetProvider;
import com.unn.engine.dataset.openml.OpenMLLocator;

public class OuterDatasetLoader {
	
	public OuterDatasetLoader() {}
	
	public OuterDataset load (DatasetLocator locator) throws Exception {
		if (locator instanceof OpenMLLocator) {
			return new OpenMLDatasetProvider(locator)
					.init()
					.load();
		} else if (locator instanceof FilesystemLocator) {
			return new FilesystemDatasetProvider(locator)
					.init()
					.load();
		} else if (locator instanceof DatacenterLocator) {
			return new DatacenterDatasetProvider(locator)
					.init()
					.load();
		} else {
			throw new Exception("[OuterDatasetFactory] Unknown locator type");
		}
	}
}
