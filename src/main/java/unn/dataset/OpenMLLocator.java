package unn.dataset;

public class OpenMLLocator extends DatasetLocator {
	int datasetId;
	
	public OpenMLLocator() {}
	
	public OpenMLLocator(int datasetId) {
		this.datasetId = datasetId;
	}

	public int getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(int datasetId) {
		this.datasetId = datasetId;
	}
	
	
}
