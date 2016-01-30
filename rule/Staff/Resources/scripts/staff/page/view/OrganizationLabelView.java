package staff.page.view;

import java.util.List;
import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;
import kz.nextbase.script.events._DoPage;
import staff.dao.OrganizationLabelDAO;
import staff.model.Organization;
import staff.model.OrganizationLabel;

/**
 * @author Kayra created 08-01-2016
 */

public class OrganizationLabelView extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		OrganizationLabelDAO dao = new OrganizationLabelDAO(session);
		String id = formData.getValueSilently("docid");
		if (!id.isEmpty()) {
			OrganizationLabel role = dao.findById(UUID.fromString(id));
			List<Organization> emps = role.getLabels();
			setContent(new _POJOListWrapper(emps, lang));
		} else {
			_ActionBar actionBar = new _ActionBar(session);
			_Action newDocAction = new _Action(getLocalizedWord("new_", lang), "", "new_organization_label");
			newDocAction.setURL("Provider?id=organizationlabel-form");
			actionBar.addAction(newDocAction);
			actionBar.addAction(new _Action(getLocalizedWord("del_document", lang), "", _ActionType.DELETE_DOCUMENT));

			setContent(actionBar);
			setContent(getViewPage(dao, formData));
		}
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {

	}
}
