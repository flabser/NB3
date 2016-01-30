package kz.flabs.runtimeobj.document.task;

import kz.flabs.util.adapters.DateAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement(name = "executor")
@XmlAccessorType(XmlAccessType.FIELD)
public class Executor implements Serializable {
	public int num;

    @XmlElement(name = "usrid")
	public String ID;

    @XmlTransient
    public String name;

    public ExecutorType type;

    @XmlElement(name = "resauthid")
	public String resetAuthorID = "";
	public String comment = "";

    @XmlElement(name = "isres")
    public boolean isReset;

    @XmlElement(name = "resp")
	public int responsible;

    @XmlTransient
	private static final long serialVersionUID = 1033256535647706979L;

    @XmlElement(name = "rdate")
    @XmlJavaTypeAdapter(DateAdapter.class)
	private Date resetDate;

    @XmlTransient
	private int execper;

    @XmlEnum
    public static enum ExecutorType {
        @XmlEnumValue("1") INTERNAL,
        @XmlEnumValue("2") EXTERNAL
    }

    public Executor(String id, ExecutorType type){
		this.ID = id;
        this.type = type;
	}

    public Executor internalExecutor(String id) {
        return new Executor(id, ExecutorType.INTERNAL);
    }

    public Executor externalExecutor(String id) {
        return new Executor(id, ExecutorType.EXTERNAL);
    }

	public Executor() {
		
	}

	public void setResetDate(Date d){
		resetDate = d;		
	}
	
	public String getID() {
		return ID;
	}
	
	public int getPercentOfExecution(){
		return this.execper;
	}
	
	public void setPercentOfExecution(int percent){
		this.execper = percent;
	}
		
	public void setID(String userID) {
		this.ID = userID;
	}
	
	public Date getResetDate(){
		return (resetDate != null ? resetDate : null);
	}
	
	
	public void setResponsible(int resp) {
		responsible = resp;
	}
	
	public int getResponsible() {
		return responsible;
	}
	
	public String toString(){
		return "executor:" + ID + ",num:" + num + ",isReset:" + isReset;
	}

	public String getResetDateAsDbFormat() {
		return null;
	}


}
