package unn.dataset;

public class OuterDatasetFactory {
	
	public OuterDatasetFactory() {}
	
	public OuterDataset load (DatasetLocator locator) throws Exception {
		if (locator instanceof OpenMLLocator) {
			return new OpenMLDatasetProvider().load();
		} else if (locator instanceof FilesystemLocator) {
			return new FilesystemDatasetProvider().load();
		} else {
			throw new Exception("[OuterDatasetFactory] Unknown locator type");
		}
	}
}
