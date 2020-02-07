package unn.session.actors;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import unn.session.Session;
import unn.session.actions.ActionResult;
import unn.session.actions.LoadDatasetAction;
import unn.session.actions.SaveModelAction;

public class PersistenceActor extends Actor {
	SaveModelAction action;
	
	public PersistenceActor(SaveModelAction action) {
		this.action = action;
	}

	public ActionResult write() {		
		WriteObjectToFile(this.action.getSession(), this.action.getPathTemplate());
		return null;
	}
	
	public Session read() {		
		Session session = ReadObjectFromFile(this.action.getPathTemplate());
		return session;
	}
	
	public void WriteObjectToFile(Object serObj, String fpath) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fpath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            System.out.println("The Object  was succesfully written to a file");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	public Session ReadObjectFromFile(String fpath) {
		try { 
            // Reading the object from a file 
            FileInputStream file = new FileInputStream(fpath); 
            ObjectInputStream in = new ObjectInputStream(file); 
  
            Session object = (Session) in.readObject(); 
  
            in.close(); 
            file.close(); 
            System.out.println("Object has been deserialized\n"
                                + "Data after Deserialization.");
            return object;
        } catch (IOException ex) { 
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) { 
        	ex.printStackTrace();
        }
		return null;
	}

	
}
