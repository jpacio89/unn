package unn.dataset;

import unn.dataset.datacenter.DatacenterDatasetProvider;
import unn.dataset.datacenter.DatacenterLocator;
import unn.dataset.filesystem.FilesystemDatasetProvider;
import unn.dataset.filesystem.FilesystemLocator;
import unn.dataset.openml.OpenMLDatasetProvider;
import unn.dataset.openml.OpenMLLocator;

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
