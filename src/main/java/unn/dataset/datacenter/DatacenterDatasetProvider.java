package unn.dataset.datacenter;

import unn.dataset.DatasetLocator;
import unn.dataset.filesystem.FilesystemDatasetSource;
import unn.dataset.filesystem.FilesystemLocator;
import unn.dataset.OuterDataset;

public class DatacenterDatasetProvider {
	DatacenterLocator locator;

	public DatacenterDatasetProvider(DatasetLocator locator) {
		this.locator = (DatacenterLocator) locator;
	}

	public DatacenterDatasetProvider init() {
		return this;
	}

	public OuterDataset load() {
		try {
			DatacenterDatasetSource source = new DatacenterDatasetSource(locator.getOptions());
			OuterDataset dataset = source.next();
			return dataset;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
