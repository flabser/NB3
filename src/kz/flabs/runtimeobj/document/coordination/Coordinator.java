package kz.flabs.runtimeobj.document.coordination;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.util.adapters.DateAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@XmlAccessorType(XmlAccessType.FIELD)
public class Coordinator extends AbstractComplexObject implements Serializable{
	@XmlElement(name = "type")
    int type;

    private Employer user;

    @XmlElement(name = "current")
	private boolean isCurrent;

    @XmlElement
	private Decision decision = new Decision();

    @XmlElement(name = "num")
    public int num;

    @XmlElement(name = "coorddate")
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date coordDate;

    private transient IDatabase db;

    @XmlTransient
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private ArrayList<Integer> attachID = new ArrayList<>();
    @XmlTransient
    public HashMap<String, BlobField> blobFieldsMap = new HashMap<String, BlobField>();

    @Deprecated
    public Coordinator() {
    }

	public Coordinator(IDatabase db) {
		this.db = db;
	}

	public Coordinator(IDatabase db, String userID) {
		this.db = db;
		user = db.getStructure().getAppUser(userID);
	}

    public ArrayList<Integer> getAttachID() {
        return attachID;
    }

    public void addAttachID(int id) {
        attachID.add(id);
    }

    public void setAttachID(ArrayList<Integer> attachID) {
        this.attachID = attachID;
    }

	public void setType(int coordinatorTypeSigner) {
		type = 	coordinatorTypeSigner;	
	}

	public String getUserID() {
		return user.getUserID();
	}

	public void setUserID(String userID) {
        user = db.getStructure().getAppUser(userID);
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public void isCurrent(int parseInt) {
       this.isCurrent = parseInt == 1;
	}

	public void setCoorDate(Date date) {
        this.coordDate = date;
	}

	public void setComment(String textContent) {
		decision.comment = textContent;
	}   

	public void setDecision(Decision d) {
		decision = d;	
		
		isCurrent = false;
	}
	
	public void setDecisionDate(Date date) {
		decision.decisionDate = date;
	}

	public int getCoordType() {
		return this.type;
	}

	public String getCoordNumber() {
		return String.valueOf(num);
	}

	public Date getDecisionDate() {
		return decision.decisionDate;
	}

	public Date getCoorDate() {
		return this.coordDate;
	}

	public Employer getEmployer() {
        return user;
	}

	public void setNumber(int num) {
		this.num = num;
		
	}

	public Decision getDecision() {
		return decision;
	}


    @Override
    public void init(IDatabase db, String initString) throws ComplexObjectException {

    }

    @Override
    public String getContent() {
        return null;
    }
}
