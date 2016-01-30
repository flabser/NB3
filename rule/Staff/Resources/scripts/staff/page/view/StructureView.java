package staff.page.view;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;
import kz.nextbase.script.events._DoPage;
import staff.dao.OrganizationDAO;
import staff.model.Organization;

public class StructureView extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		List<_IXMLContent> content = new ArrayList<_IXMLContent>();
		OrganizationDAO dao = new OrganizationDAO(session);
		Organization org = dao.findPrimaryOrg();
		if (org != null) {
			content.add(new _POJOObjectWrapper(org, lang));
		} else {
			content.add(new _POJOListWrapper(getLocalizedWord("no_primary_org", lang)));
		}

		_ActionBar actionBar = new _ActionBar(session);
		_Action newDocAction = new _Action(getLocalizedWord("new_", lang), "", "new_organization");
		newDocAction.setURL("Provider?id=organization-form");
		actionBar.addAction(newDocAction);
		actionBar.addAction(new _Action(getLocalizedWord("del_document", lang), "", _ActionType.DELETE_DOCUMENT));

		setContent(actionBar);
		setContent(content);
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {

	}
}
