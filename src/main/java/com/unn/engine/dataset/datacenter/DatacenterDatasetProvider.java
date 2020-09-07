package com.unn.engine.dataset.datacenter;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;

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
