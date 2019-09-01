package unn;

import java.util.ArrayList;
import java.util.HashMap;

public interface IEnvironment {
	
	ArrayList<IOperator> getInputs(String spaceId);
	
	Integer mapInput(String inputString, String version);
	
	Double predict(String spaceId, HashMap<IOperator, Integer> values);
	
	void init() throws Exception;
}
