package kz.flabs.runtimeobj.document.project;

import java.io.Serializable;
import java.util.Date;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.document.coordination.Decision;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;

public class Coordinator implements ICoordConst, Serializable {
	public String userID;
	public int type = COORDINATOR_TYPE_UNDEFINED;
	public int num;
	public int decision;
	public String comment = "";
	public int isCurrent;
	public String notesID;
	public Date coorDate;
	public boolean isFromWebForm = false;
	private Block block;
	private static final long serialVersionUID = 1L;	

	private Date decisionDate;

	public Coordinator(Block block){
		this.block = block;
	}

	public Coordinator(String user){
		this.userID = user;
	}

	public void setCoordType(int t){	
		type = t;
	}

	public int getCoordType(){	
		return type;
	}

	public void resetCoordinator() {
		this.setDecision(new Decision(DECISION_UNKNOWN, ""));
		this.setCoorDate(null);
	}

	public void setCoordNumber(int t){	
		num = t;
	}

	public int getCoordNumber(){	
		return num;
	}

	public Block getParentBlock() {
		return block;
	}

	public void setCurrent(boolean c){
		if (c){
			isCurrent = 1;
			coorDate = new Date();
		}else{
			coorDate = null;
			isCurrent = 0;
		}
	}

	public void setCurrentRecipient(int c){
		isCurrent = c;
		if (c == 1){
			coorDate = new Date();
		}else{
			coorDate = null;
		}	
	}

	public void isCurrent(boolean c) {
		if (c) {
			isCurrent = 1;
		} else {
			isCurrent = 0;
		}
	}

	public void isCurrent(int c) {
		isCurrent = c;
	}

	public int isCurrent(){
		return isCurrent;			 
	}

	public String getUser(){    
		return this.userID;
	}

	public int getDecis(){		
		return decision;
	}
	
	public void setDecision(Decision d) {
		decision = d.decision;
		comment = d.comment;
		decisionDate = d.decisionDate;
		isCurrent = 0;
	}

	public void setComment(String remark){
		comment = remark;
	}

	public String getComment(){
		return this.comment;
	}

	public void setCoorDate(Date d){
		coorDate = d;
	}

	public String getCoorDateAsDbFormat(){
		if (coorDate != null){
			return "'" + Database.sqlDateTimeFormat.format(coorDate) + "'";
		}else{
			return "null";
		}
	}

	public Date getCoorDate(){
		return this.coorDate;
	}

	public void setDecisionDate(Date d){
		decisionDate = d;
	}

	public String getDecisionDateAsDbFormat(){
		if (decisionDate != null){
			return "'" + Database.sqlDateTimeFormat.format(decisionDate) + "'";
		}else{
			return "null";
		}
	}

	public Date getDecisionDate(){
		return this.decisionDate;
	}

	public String toString(){
		return "user=" + userID + ", type=" + type + ", iscurrent=" + isCurrent + ", notesid=" + notesID; 
	}

	public boolean equals(Object o) {
		if (!(o instanceof Coordinator)) return false;
		if (this.userID.equals(((Coordinator)o).userID)) return true;
		return false;
	}

	public String toXML() {
		/*Employer emp = env.getDataBase().getStructure().getAppUser(userID);		

		if(emp != null){						
			toPublish.add(new ScriptShowField(entryName, userID, emp.getFullName()));							
		}else{
			AppEnv.logger.warningLogEntry("Employer  \"" + userID + "\" has not found");
		}	*/
		return userID;
	}

}
