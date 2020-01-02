package unn.dataset;

import java.util.ArrayList;

public class OuterDataset {
	ArrayList<String> header;
	ArrayList<ArrayList<String>> body;
	
	public OuterDataset() {
		this.header = new ArrayList<String>();
		this.body = new ArrayList<ArrayList<String>>();
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
	
	public void addRow(ArrayList<String> row) {
		this.body.add(row);
	}
}
