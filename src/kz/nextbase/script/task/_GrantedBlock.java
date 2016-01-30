package kz.nextbase.script.task;


import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.GrantedBlock;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script.struct._Employer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class _GrantedBlock implements _IXMLContent {

    private GrantedBlock grantedBlock;
    private _Session session;

    public _GrantedBlock(_Session session) {
        this.session = session;
        grantedBlock = new GrantedBlock();
    }

    public _GrantedBlock(_Session session, GrantedBlock grantedBlock) {
        this.grantedBlock = grantedBlock;
        this.session = session;
    }

    public void addGrantUsers(String[] users) {
        _Employer grantUser = null;
        ArrayList<Employer> grantUsers = new ArrayList<>();
        for (String user : users) {
            try {
                grantUser = session.getStructure().getEmployer(user);
                grantUsers.add(grantUser.employer);
            } catch (DocumentException e) {
                AppEnv.logger.errorLogEntry(e);
            }
        }
        grantedBlock.addGrantUsers(grantUsers);
        grantedBlock.setGrantdate(Calendar.getInstance(TimeZone.getDefault()));
    }

    public void setGrantor(_Employer grantor) {
        grantedBlock.setGrantor(grantor);
    }

    public void addGrantUser(String userID) {
        try {
            _Employer grantUser = session.getStructure().getEmployer(userID);
            grantedBlock.addGrantUser(grantUser);
        } catch (DocumentException e) {
            AppEnv.logger.errorLogEntry(e);
        }
    }

    public GrantedBlock getBaseObject() {
        return grantedBlock;
    }

    @Override
    public String toXML() throws _Exception {
        StringBuffer xmlContent = new StringBuffer(10000);

        xmlContent.append("<grantdate>" + (grantedBlock.getGrantdate() != null ? new SimpleDateFormat().format(grantedBlock.getGrantdate().getTime()) : "") + "</grantdate>");
        xmlContent.append("<grantor userid=\"" + grantedBlock.grantor.getUserID() + "\">" + grantedBlock.grantor.getFullName() + "</grantor>");
        xmlContent.append("<grantusers>");
        ArrayList<Employer> c = grantedBlock.grantUsers;
        for (int i = 0; i < c.size(); i++) {
            xmlContent.append("<entry userid=\"" + c.get(i).getUserID() + "\">" + c.get(i).getFullName() + "</entry>");
        }
        xmlContent.append("</grantusers>");

        return xmlContent.toString();
    }
}
