package unn.session.actors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import plugins.openml.JobConfig;
import plugins.openml.MiningEnvironment;
import unn.dataset.InnerDataset;
import unn.interfaces.IEnvironment;
import unn.interfaces.IOperator;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.LoadModelAction;
import unn.structures.Config;
import unn.structures.VTR;

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
