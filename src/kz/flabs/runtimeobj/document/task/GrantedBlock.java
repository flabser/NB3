package kz.flabs.runtimeobj.document.task;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.util.adapters.CalendarAdapter;
import kz.nextbase.script.struct._Employer;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

@XmlAccessorType(XmlAccessType.FIELD)
public class GrantedBlock extends AbstractComplexObject implements Serializable {

    @XmlTransient
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "grantdate")
    @XmlJavaTypeAdapter(CalendarAdapter.class)
    public Calendar grantdate;

    @XmlAttribute
    public String className = GrantedBlock.class.getName();

    @XmlElement(name = "grantor")
    public Employer grantor;

    @XmlElement(name = "grantuser")
    public ArrayList<Employer> grantUsers = new ArrayList<>();

    @Override
    public void init(IDatabase db, String initString) throws ComplexObjectException {

    }

    @Override
    public String getContent() {
        return null;
    }

    public Calendar getGrantdate() {
        return grantdate;
    }

    public void setGrantdate(Calendar grantdate) {
        this.grantdate = grantdate;
    }

    public void addGrantUser(_Employer grantUser) {
        grantUsers.add(grantUser.employer);
    }

    public void addGrantUsers(ArrayList grantUsers) {
        this.grantUsers.addAll(grantUsers);
    }

    public void setGrantor(_Employer grantor) {
        this.grantor = grantor.employer;
    }
}
