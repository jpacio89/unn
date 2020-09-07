package com.unn.engine.session.actors;

import java.io.BufferedReader;
import java.io.FileReader;

import com.unn.engine.session.actions.ActionResult;
import com.unn.engine.session.actions.LoadModelAction;

public class LoadModelActor extends Actor {
	LoadModelAction action;
	
	public LoadModelActor(LoadModelAction action) {
		this.action = action;
	}

	public ActionResult write() {
		
		
    	return null;
	}

	
	public static void parseFromFile(String filePath) {		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
		    String line;
		    int n = -1;
		    
		    String metadata = br.readLine();
		    
		    while ((line = br.readLine()) != null) { 	
		    	
		    }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
