package com.unn.engine.dataset.filesystem;

import com.unn.engine.dataset.DatasetLocator;

public class FilesystemLocator extends DatasetLocator {
	String path;
	
	public FilesystemLocator() {}
	
	public FilesystemLocator(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
