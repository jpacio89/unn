package com.unn.engine.dataset.filesystem;

import com.unn.engine.dataset.DatasetLocator;
import com.unn.engine.dataset.OuterDataset;

public class FilesystemDatasetProvider {
	FilesystemLocator locator;
	
	public FilesystemDatasetProvider(DatasetLocator locator) {
		this.locator = (FilesystemLocator) locator;
	}

	public FilesystemDatasetProvider init() {
		return this;
	}

	public OuterDataset load() {
		try {
			FilesystemDatasetSource source = new FilesystemDatasetSource(locator.getPath());
			OuterDataset dataset = source.next();
			return dataset;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
