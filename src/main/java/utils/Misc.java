package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Misc {

	public static String md5 (String input)
	{
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			
			md.update(input.getBytes());
			byte[] digest = md.digest();
			
			return byteArrayToHex(digest);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String byteArrayToHex(byte[] a) {
	   StringBuilder sb = new StringBuilder(a.length * 2);
	   for(byte b : a) {
	      sb.append(String.format("%02x", b & 0xff));
	   }
	   return sb.toString();
	}
	
	public static boolean isSubset(int arr1[], int arr2[]) {
		int m = arr1.length; 
        int n = arr2.length; 
	    int i = 0; 
	    int j = 0; 
	    for (i = 0; i < n; i++) { 
	        for (j = 0; j < m; j++) 
	            if(arr2[i] == arr1[j]) 
	                break; 
	          
	        /* If the above inner loop  
	        was not broken at all then 
	        arr2[i] is not present in 
	        arr1[] */
	        if (j == m) 
	            return false; 
	    } 
	      
	    /* If we reach here then all 
	    elements of arr2[] are present 
	    in arr1[] */
	    return true; 
	}
}
