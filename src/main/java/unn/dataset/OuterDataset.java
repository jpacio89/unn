package unn.dataset;

import java.util.ArrayList;
import java.util.Collections;

public class OuterDataset {
	ArrayList<String> header;
	ArrayList<ArrayList<String>> body;
	
	public OuterDataset() {
		this.header = new ArrayList<String>();
		this.body = new ArrayList<ArrayList<String>>();
	}
	
	public void setHeader(String[] features) {
		this.header.clear();
		Collections.addAll(this.header, features);
	}
	
	public int featureCount() {
		return this.header.size();
	}
	
	public int sampleCount() {
		return this.body.size();
	}
	
	public ArrayList<String> getHeader() {
		return this.header;
	}
	
	public ArrayList<String> getRow(int index) {
		return this.body.get(index);
	}
	
	public void addSample(String[] sample) {
		ArrayList<String> sampleCollection = new ArrayList<String>();
		Collections.addAll(sampleCollection, sample);
		this.body.add(sampleCollection);
	}
	
	public void addSample(ArrayList<String> sample) {
		this.body.add(sample);
	}
}
