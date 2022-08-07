package com.unn.engine.dataset.filesystem;

import com.unn.engine.dataset.OuterDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FilesystemDatasetSource {
	String path;
	
	public FilesystemDatasetSource(String path) {
		// this.path = "/Users/joaocoelho/Documents/Work/UNN/com.unn/com.unn-extras/wav-analyzer/dataset.csv";
		this.path = path;
	}
	
	public OuterDataset next() {
		File f = new File(this.path);
		return buildOuterDataset(f);
	}
	
	private OuterDataset buildOuterDataset(File csv) {
		final String separator = ",";
		
		try {
			OuterDataset dataset = new OuterDataset();
			BufferedReader reader = new BufferedReader(new FileReader(csv));
			
			String header;
			do {
				header = reader.readLine();
				if (header.startsWith("#")) {
					header = null;
				}
			} while (header == null);

			dataset.setHeader(header.split(separator));
			String line = header;
			
			while (line != null) {
				line = reader.readLine();
				
				if (line == null) {
					continue;
				}
				
				String[] sample = line.split(separator);
				
				if (sample.length != dataset.featureCount()) {
					System.err.println(String.format("[OpenMLDatasetProvider %d !== %d] Invalid line: %s",
							sample.length, dataset.featureCount(), line));
					continue;
				}
				
				dataset.addSample(sample);
			}
			
			reader.close();
			return dataset;
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
