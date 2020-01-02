package unn.dataset;

public class OuterDatasetFactory {
	
	public OuterDatasetFactory() {}
	
	public OuterDataset load (DatasetLocator locator) throws Exception {
		if (locator instanceof OpenMLLocator) {
			return new OpenMLDatasetProvider(locator)
					.init()
					.load();
		} else if (locator instanceof FilesystemLocator) {
			return new FilesystemDatasetProvider(locator)
					.init()
					.load();
		} else {
			throw new Exception("[OuterDatasetFactory] Unknown locator type");
		}
	}
}
