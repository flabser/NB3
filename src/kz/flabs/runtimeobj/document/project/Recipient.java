package kz.flabs.runtimeobj.document.project;

import java.io.Serializable;

public class Recipient implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int recordID;
	private String userID;
	
	public Recipient(){
		
	}
	
	Recipient(String u){
		userID = u;
	}
	
	public int getRecordID() {
		return recordID;
	}

	public void setRecordID(int recordID) {
		this.recordID = recordID;
	}

	public void setUserID(String u){
		userID = u;;
	}
	
	public String getUserID(){
		return userID;
	}
	
	public String toString(){
		return "userID=" + userID;
	}
	
}
