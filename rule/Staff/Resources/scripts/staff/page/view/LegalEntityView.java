package staff.page.view;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;
import kz.nextbase.script.events._DoPage;
import staff.dao.OrganizationDAO;

/**
 * @author Kayra created 04-01-2016
 */

public class LegalEntityView extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		_ActionBar actionBar = new _ActionBar(session);
		_Action newDocAction = new _Action(getLocalizedWord("new_", lang), "", "new_organization");
		newDocAction.setURL("Provider?id=organization-form");
		actionBar.addAction(newDocAction);
		actionBar.addAction(new _Action(getLocalizedWord("del_document", lang), "", _ActionType.DELETE_DOCUMENT));

		setContent(actionBar);
		setContent(getViewPage(new OrganizationDAO(session), formData));
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {

	}
}
