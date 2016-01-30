package workspace.form.reg_legalentity

import kz.nextbase.script._Document
import kz.nextbase.script._Session
import kz.nextbase.script.events._FormPostSave

class PostSave extends _FormPostSave {


    @Override
    void doPostSave(_Session ses, _Document doc) {
        //def emp  = (_Employer)doc;1

        def g = ses.getStructure().getGroup(ses, "[rent_viewer]", ["[supervisor]"] as Set, "[supervisor]");
        def l = g.getListOfMembers();
        def emp = ses.getStructure().getUserByCondition("empid=${doc.getDocID()}")
        l.add(emp.getUserID());
        g.setListOfMembers(l as String[]);
        g.save("[supervisor]");

    }
}
