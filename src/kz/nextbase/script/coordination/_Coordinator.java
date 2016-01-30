package kz.nextbase.script.coordination;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.coordination.Coordinator;
import kz.flabs.runtimeobj.document.coordination.Decision;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.util.Util;
import kz.nextbase.script._BlobField;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script.constants._DecisionType;
import kz.nextbase.script.struct._Employer;

import java.util.ArrayList;
import java.util.Date;

public class _Coordinator implements _IXMLContent, ICoordConst {
	private Coordinator co;

	
	public _Coordinator(IDatabase db){
		co = new Coordinator(db);
	}
	
	public _Coordinator(_Session ses, Coordinator c) {
		co = c;	
	}

	_Employer getUser(){
		Employer emp = co.getEmployer();
		return new _Employer(emp);
		
	}
	
	String getUserID(){
		return co.getUserID();
		
	}

    public String getComment() {
        return co.getDecision().getComment();
    }

	public void setAsSigner(){
		co.setType(ICoordConst.COORDINATOR_TYPE_SIGNER);
	}

	public void setAsReviewer(){
		co.setType(ICoordConst.COORDINATOR_TYPE_REVIEWER);
	}

    public Decision getDecision() {
        return co.getDecision();
    }

    public _DecisionType getDecisionType() {
        switch (co.getDecision().decision) {
            case DECISION_YES:
                return _DecisionType.AGREE;
            case DECISION_NO:
                return  _DecisionType.DISAGREE;
            case DECISION_UNKNOWN:
                default:
                    return _DecisionType.UNDEFINED;
        }
    }

    public void setDecision(_DecisionType decision, String comment) {
        switch (decision) {
            case AGREE:
                co.setDecision(new Decision(DECISION_YES, comment));
                break;
            case DISAGREE:
                co.setDecision(new Decision(DECISION_NO, comment));
                break;
            case UNDEFINED:
                co.setDecision(new Decision(DECISION_UNKNOWN, comment));
                break;
        }
    }

	void setCurrent(boolean isCurrent){
		co.setCurrent(isCurrent);
	}

    public void addAttachID(int id) {
        if (id != 0) {
            co.addAttachID(id);
        }
    }

    public ArrayList<Integer> getAttachID() {
        return co.getAttachID();
    }

    public void setAttachID(ArrayList<Integer> ids) {
        co.setAttachID(ids);
    }

	public void setUserID(String coordinator) {
		co.setUserID(coordinator);
		
	}

	public String toXML() throws _Exception {
		StringBuffer xmlContent = new StringBuffer(10000);

		xmlContent.append(getUser().toXML());
		xmlContent.append("<iscurrent>" + co.isCurrent() +  "</iscurrent>");
		Decision d =  getDecision();
		if (d.decision != DECISION_UNKNOWN){
			switch(d.decision){
				case DECISION_YES:
					xmlContent.append("<decision>" + _DecisionType.AGREE +  "</decision>");
					break;
				case DECISION_NO:
					xmlContent.append("<decision>" + _DecisionType.DISAGREE +  "</decision>");
					break;
				default:
					xmlContent.append("<decision>" + _DecisionType.UNDEFINED +  "</decision>");
			}
			xmlContent.append("<comment>" + d.getComment() +  "</comment>");

            for (BlobField field : co.blobFieldsMap.values()) {
                xmlContent.append(new _BlobField(field).toXML());
            }

			xmlContent.append("<decisiondate>" + Util.convertDataTimeToStringSilently(d.getDecisionDate()) +  "</decisiondate>");
		}		
		return xmlContent.toString();
	

	}

    public void setCoorDate(Date date) {
        this.co.setCoorDate(date);
    }

    public void resetCoordinator() {
        this.setDecision(_DecisionType.UNDEFINED, "");
        this.co.setDecisionDate(null);
        this.setCoorDate(null);
        this.setAttachID(new ArrayList<Integer>());
    }

	public Coordinator getBaseObject() {
		return co;
	}
}
