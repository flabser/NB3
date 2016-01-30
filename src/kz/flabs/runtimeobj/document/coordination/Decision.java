package kz.flabs.runtimeobj.document.coordination;

import kz.flabs.util.adapters.DateAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
public class Decision implements ICoordConst, Serializable{

    @XmlElement
    public int decision = DECISION_UNKNOWN;

    @XmlElement
	public String comment = "";

    @XmlElement(name = "decisiondate")
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date decisionDate;

    private static final long serialVersionUID = 1L;

	public Decision(int d, String c){
		decision = d;
		comment = c;
		decisionDate = new Date();
	}

	public Decision(int d, String c, Date dd) {
		decision = d;
		comment = c;
		decisionDate = dd;
	}

	public Decision() {
		
	}

	public String getComment() {
		if(comment != null){
			return comment;
		}else{
			return "";
		}
	}

	public Date getDecisionDate() {
		return decisionDate;
	}

    public void setDecisionDate(Date date) {
        decisionDate = date;
    }

}
