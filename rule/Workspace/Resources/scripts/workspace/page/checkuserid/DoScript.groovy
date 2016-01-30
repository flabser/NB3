package workspace.page.checkuserid

import kz.nextbase.script._Session
import kz.nextbase.script._Tag
import kz.nextbase.script._WebFormData
import kz.nextbase.script._XMLDocument
import kz.nextbase.script.events._DoScript

class DoScript extends _DoScript{
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {
        String userid = formData.getValueSilently("login")
        def struct = session.getStructure();
        def emp = struct.getEmployer(userid);

        publishElement(new _XMLDocument(new _Tag("isLoginFree", emp.isNew())))
	}
}
