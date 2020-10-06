package com.unn.engine.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.unn.engine.dataset.FeatureValueHistogram;
import com.unn.engine.Config;
import com.unn.engine.functions.FunctionDescriptor;
import com.unn.engine.functions.Raw;
import com.unn.engine.interfaces.IFunctor;
import com.unn.engine.utils.Pair;

public class NumericMapper extends ValuesDescriptor implements Serializable {
	private static final long serialVersionUID = 1744475762857207392L;
	public int groupCount;
	public ArrayList<Double> possibleValues;
	public ArrayList<Pair<Double, Double>> mapperBounds;
	public FeatureValueHistogram histogram;
	
	public NumericMapper() {
		
	}
	
	public void init(int groupCount, ArrayList<Double> possibleValues) {
		this.histogram = new FeatureValueHistogram();
		this.groupCount = groupCount;
		this.possibleValues = new ArrayList<Double>(possibleValues);
		this.findDomain();
		this.buildHistogram();
	}
	
	public Integer getInnerValue(double outerValue) {
		int index = 0;
		for (Pair<Double, Double> bound : this.mapperBounds) {
			if (outerValue < bound.first()) {
				break;
			}
			index++;
			if (outerValue < bound.second()) {
				break;
			}
		}
		int range = Config.STIM_MAX - Config.STIM_MIN + 1;
		int step = Math.max(1, range / (this.groupCount + 2));
		int innerVal = Config.STIM_MIN + step * index;
		return innerVal;
	}
	
	public ArrayList<Integer> getAllInnerValues() {
		ArrayList<Integer> values = new ArrayList<Integer>();
		int range = Config.STIM_MAX - Config.STIM_MIN + 1;
		int step = Math.max(1, range / (this.groupCount + 2));
		
		for(int i = 0; i < this.mapperBounds.size() + 1; ++i) {
			int innerVal = Config.STIM_MIN + step * i;
			values.add(innerVal);
		}
		
		return values;
	}
	
	private void buildHistogram() {
		this.histogram.occurences.clear();
		
		for (Double outerValue : this.possibleValues) {
			Integer innerValue = getInnerValue(outerValue);
			if (this.histogram.occurences.containsKey(innerValue)) {
				Integer counter = this.histogram.occurences.get(innerValue) + 1;
				this.histogram.occurences.put(innerValue, counter);
			} else {
				this.histogram.occurences.put(innerValue, 1);
			}
		}
		
		this.histogram.minimum = Collections.min(this.possibleValues);
		this.histogram.maximum = Collections.max(this.possibleValues);
	}
	
	private void findDomain() {
		ArrayList<Double> distinctValues = new ArrayList<Double>(new HashSet<Double>(this.possibleValues));
		Collections.sort(distinctValues);
		
		int avgCardinality = Math.max(1, distinctValues.size() / this.groupCount);
		double lowest = Collections.min(distinctValues);
		double lowerBound = lowest;

		this.mapperBounds = new ArrayList<Pair<Double, Double>>();
		
		for (int i = 0; i < this.groupCount; ++i) {
			int nextIndex = (i+1) * avgCardinality;
			double upperBound;
			if (nextIndex >= distinctValues.size()) {
				upperBound = distinctValues.get(distinctValues.size() - 1);
				mapperBounds.add(new Pair<Double, Double>(lowerBound, upperBound));	
				break;
			} else {
				upperBound = distinctValues.get(nextIndex);
				mapperBounds.add(new Pair<Double, Double>(lowerBound, upperBound));	
			}
			lowerBound = upperBound;
		}
	}

	@Override
	public ArrayList<String> getGroups(String suffix) {
		ArrayList<String> groups = new ArrayList<>();
		for (int i = 0; i < this.mapperBounds.size(); ++i) {
			groups.add(String.format("numeric_%d_%s", i, suffix));
		}
		return groups;
	}

	@Override
	public IFunctor getFunctorByGroup(String group) {
		Raw raw = new Raw();
		raw.setDescriptor(new FunctionDescriptor(".", group,-1));
		return raw;
	}

	@Override
	public String getGroupByOuterValue(String outerFeatureValue, String suffix) {
		Integer innerValue = getInnerValue(Double.parseDouble(outerFeatureValue));
		return String.format("numeric_%d_%s", innerValue, suffix);
	}

}
