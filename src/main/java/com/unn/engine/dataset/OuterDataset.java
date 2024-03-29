package com.unn.engine.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class OuterDataset implements Serializable {
	private static final long serialVersionUID = 8073990196346689566L;
	ArrayList<String> header;
	ArrayList<ArrayList<String>> body;
	
	public OuterDataset() {
		this.header = new ArrayList<>();
		this.body = new ArrayList<>();
	}
	
	public void setHeader(String[] features) {
		this.header.clear();
		Collections.addAll(this.header, features);
	}

	public void setHeader(ArrayList<String> features) {
		this.header.clear();
		this.header.addAll(features);
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
	
	public ArrayList<ArrayList<String>> getBody() {
		return body;
	}

	public void setBody(ArrayList<ArrayList<String>> body) {
		this.body = body;
	}

	public ArrayList<String> getSample(int index) {
		return this.body.get(index);
	}

	public HashMap<String, String> getSampleAsMap(int index) {
		ArrayList<String> sample = this.body.get(index);
		HashMap<String, String> sampleMap = new HashMap<>();
		
		for (int i = 0; i < sample.size(); ++i) {
			String featureValue = sample.get(i);
			String featureName = header.get (i);
			sampleMap.put(featureName, featureValue);
		}
		
		return sampleMap;
	}
	
	public void addSample(String[] sample) {
		ArrayList<String> sampleCollection = new ArrayList<>();
		Collections.addAll(sampleCollection, sample);
		this.body.add(sampleCollection);
	}
	
	public void addSample(ArrayList<String> sample) {
		this.body.add(sample);
	}

	public int getFeatureIndex(String feature) {
		return this.header.indexOf(feature);
	}

	public String getFeatureAtSample(int sampleIndex, int featureIndex) {
		return this.body.get(sampleIndex).get(featureIndex);
	}
}
