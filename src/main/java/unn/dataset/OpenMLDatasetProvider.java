package unn.dataset;

import java.io.File;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

public class OpenMLDatasetProvider extends DatasetProvider {
	final String apiKey = "afd8250e50b774f1cd0b4a4534a1ae90";
	
	OpenMLLocator locator;
	OpenmlConnector client;
	
	public OpenMLDatasetProvider(DatasetLocator locator) {
		this.locator = (OpenMLLocator) locator;
	}
	
	public OpenMLDatasetProvider init() {
		this.client = new OpenmlConnector(apiKey);
		return this;
	}

	public OuterDataset load() {
		try {
			DataSetDescription data = this.client.dataGet(locator.getDatasetId());
			File csv = this.client.datasetGetCsv(data);
			FilesystemDatasetSource source = new FilesystemDatasetSource(csv.getAbsolutePath());
			OuterDataset dataset = source.next();
			return dataset;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
