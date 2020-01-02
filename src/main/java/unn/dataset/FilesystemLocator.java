package unn.dataset;

public class FilesystemLocator extends DatasetLocator {
	String path;
	
	public FilesystemLocator() {}
	
	public FilesystemLocator(String path) {
		this.path = path;
	}
}
