package kz.nextbase.script.project;

import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.coordination.Decision;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.nextbase.script.constants._DecisionType;

import java.util.Date;
@Deprecated
public class _Coordinator implements ICoordConst {
	public Coordinator coordinator;
		
	
	public String getUserID() {
		return coordinator.userID;
	}

	public void setUserID(String userID) {
		this.coordinator.userID = userID;
	}

	public String getEmail() {
		ISystemDatabase sdb = DatabaseFactory.getSysDatabase();
		User user = sdb.getUser(this.getUserID());
		String email = user.getEmail();
		return ( email!= null ? email : "");
	}
	
	public String getInstMessengerAgent(){
		ISystemDatabase sdb = DatabaseFactory.getSysDatabase();
		User user = sdb.getUser(this.getUserID());
		String jid = user.getInstMsgAddress();
		return (jid != null ? jid : "");
	}

	public String getShortName() throws DocumentException{
		Employer emp = coordinator.getParentBlock().getParentProject().db.getStructure().getAppUser(coordinator.userID);
		return emp.getShortName();
	}
	
	public _Coordinator(String un){
		coordinator = new Coordinator(un);		
	}

	public _Coordinator(Coordinator coord) {
		coordinator = coord;		
		
	}

	Coordinator getBaseObject(){
		return coordinator;
	}

	public void setAsSigner(){
		coordinator.type = COORDINATOR_TYPE_SIGNER;
	}

	public void setAsReviewer(){
		coordinator.type = COORDINATOR_TYPE_REVIEWER;
	}

	public void setCoordType(String t){	
		if(t.equalsIgnoreCase("tosign")){
			coordinator.type = COORDINATOR_TYPE_SIGNER;
		} else{
			coordinator.type = COORDINATOR_TYPE_REVIEWER;
		}
	}

	public String getCoordType(){
		if(coordinator.type == COORDINATOR_TYPE_SIGNER){
			return "signer";
		} else{
			return "reviewer";
		}	
	}

		
	public void setCoordDate(String d){
		setCoordDate(Util.convertStringToDateTime(d));
	}

	public void setCoordDate(Date d){
		coordinator.coorDate = d;
	}
	
	public void setComment(String remark){
		coordinator.setComment(remark);
	}
	
	public String getComment(){
		return coordinator.getComment();
	}
	
	@Deprecated
	public void setDec(String decision, String comment){
		if(decision.equalsIgnoreCase("agree")){
			coordinator.setDecision(new Decision(DECISION_YES, comment));		
		} else if(decision.equalsIgnoreCase("disagree")){
			coordinator.setDecision(new Decision(DECISION_NO, comment));	
		}
	}

	public void setDecision(_DecisionType decision, String comment){
		if(decision == _DecisionType.AGREE){
			coordinator.setDecision(new Decision(DECISION_YES, comment));		
		} else if(decision == _DecisionType.DISAGREE){
			coordinator.setDecision(new Decision(DECISION_NO, comment));	
		}
	}
	
	public _DecisionType getDecision(){
		if (coordinator.getDecis() == DECISION_YES){
			return _DecisionType.AGREE;
		}else if (coordinator.getDecis() == DECISION_NO){
			return _DecisionType.DISAGREE;
		}else{
			return _DecisionType.UNDEFINED;
		}
	}

	public void setCurrent(boolean c){
		coordinator.setCurrent(c);
	}
	
	public boolean isCurrent(){
		if (coordinator.isCurrent() == 1){
			return true;
		}
		return false;
	}
	
	public boolean isFinished(){
		if (coordinator.isCurrent() == 0 && coordinator.getDecis() != 0){
			return true;
		}
		return false;
	}
	
	public Date getCoordDate(){
		return coordinator.getCoorDate();
	}

	public Date getDecisionDate(){
		return coordinator.getDecisionDate();
	}
	
	public void resetCoordinator(){
		coordinator.resetCoordinator();
	}
	
	public String toString(){
		return coordinator.toString();
	}
	
}
