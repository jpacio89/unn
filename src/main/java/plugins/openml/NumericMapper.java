package plugins.openml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import unn.dataset.FeatureValueHistogram;
import unn.structures.Config;
import utils.Domain;
import utils.Pair;
import utils.Range;

public class NumericMapper extends OuterValueType {
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
		int range = Config.STIMULI_MAX_VALUE - Config.STIMULI_MIN_VALUE + 1;
		int step = Math.max(1, range / (this.groupCount + 2));
		int innerVal = Config.STIMULI_MIN_VALUE + step * index;
		return innerVal;
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
}
