package utils;

import java.util.ArrayList;

public class CombinationUtils {	
	public static long factorial(int n) {
	    long fact = 1;
	    for (int i = n; i > 1; i--) {
	        fact *= i;
	    }
	    return fact;
	}
	
	public static long countCombinations(int n, int r) {
	    long fact = 1;
	    for (int i = n; i > n - r; i--) {
	        fact *= i;
	    }
		return fact / factorial(r);
	}
	
	public static long countCombinationsSum(int n, int r) {
		long sum = 0;
		for (int i = 1; i <= r; ++i) {
			sum += countCombinations(n, i);
		}
	    return sum;
	}
	
	public static ArrayList<int[]> generateCombinations (int n, int r) {
		ArrayList<int[]> combinations = new ArrayList<>();
	    int[] combination = new int[r];
	 
	    // initialize with lowest lexicographic combination
	    for (int i = 0; i < r; i++) {
	        combination[i] = i;
	    }
	 
	    while (combination[r - 1] < n) {
	    	int[] comboClone = combination.clone();
	        combinations.add(comboClone);
	 
	         // generate next combination in lexicographic order
	        int t = r - 1;
	        while (t != 0 && combination[t] == n - r + t) {
	            t--;
	        }
	        combination[t]++;
	        for (int i = t + 1; i < r; i++) {
	            combination[i] = combination[i - 1] + 1;
	        }
	    }
	 
	    return combinations;
	}
	
	public static ArrayList<ArrayList<Integer>> getSubsets(int n) {
		ArrayList<ArrayList<Integer>> subsets = new ArrayList<ArrayList<Integer>>();
		
		int[] set = new int[n];
		
		for (int i = 0; i < n; ++i) {
			set[i] = i;
		}
  
        // Run a loop for printing all 2^n 
        // subsets one by obe 
        for (int i = 0; i < (1<<n); i++) {
        	ArrayList<Integer> subset = new ArrayList<Integer>();
  
            // Print current subset 
            for (int j = 0; j < n; j++) {
            	// (1<<j) is a number with jth bit 1 
                // so when we 'and' them with the 
                // subset number we get which numbers 
                // are present in the subset and which 
                // are not 
                if ((i & (1 << j)) > 0) {
                	subset.add(set[j]);	
                }
            }
  
            if (!subset.isEmpty()) {
            	subsets.add(subset);
            	System.out.println(subset);
            }
        }
        
        return subsets;
    }
}
