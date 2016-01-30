package staff.navigator;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script._Tag;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script._XMLDocument;
import kz.nextbase.script.events._DoPage;
import kz.nextbase.script.outline._Outline;
import kz.nextbase.script.outline._OutlineEntry;
import staff.dao.OrganizationLabelDAO;
import staff.dao.RoleDAO;
import staff.model.OrganizationLabel;
import staff.model.Role;

public class MainNavigator extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		List<_IXMLContent> list = new ArrayList<_IXMLContent>();

		_Tag currentTag = new _Tag("current");
		currentTag.setAttr("id", formData.getValueSilently("id").replace("-form", "-view") + formData.getValueSilently("docid"));

		_Outline common_outline = new _Outline(getLocalizedWord("common_staff_data", lang), "common");
		common_outline.addEntry(new _OutlineEntry(getLocalizedWord("structure", lang), "structure-view"));

		_OutlineEntry employeeEntry = new _OutlineEntry(getLocalizedWord("employees", lang), "employee-view");
		for (Role role : new RoleDAO(session).findAll()) {
			employeeEntry.addEntry(new _OutlineEntry(getLocalizedWord(role.getName(), lang), getLocalizedWord("assigned", lang) + " : "
			        + getLocalizedWord(role.getName(), lang), "role-view" + role.getId(), "Provider?id=role-view&docid=" + role.getId()));
		}
		common_outline.addEntry(employeeEntry);

		_OutlineEntry orgEntry = new _OutlineEntry(getLocalizedWord("organizations", lang), "organization-view");
		for (OrganizationLabel label : new OrganizationLabelDAO(session).findAll()) {
			orgEntry.addEntry(new _OutlineEntry(getLocalizedWord(label.getName(), lang), getLocalizedWord("labeled", lang) + " : "
			        + getLocalizedWord(label.getName(), lang), "organization-label-view" + label.getId(),
			        "Provider?id=organization-label-view&docid=" + label.getId()));
		}
		common_outline.addEntry(orgEntry);

		common_outline.addEntry(new _OutlineEntry(getLocalizedWord("roles", lang), "role-view"));
		common_outline.addEntry(new _OutlineEntry(getLocalizedWord("organization_labels", lang), "organization-label-view"));

		_Outline specific_outline = new _Outline(getLocalizedWord("specific_staff_data", lang), "specific");
		specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("contractors", lang), "contractor-view"));
		specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("individuals", lang), "individual-view"));
		specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("legal_entities", lang), "legal-entity-view"));
		specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("responsible_persons", lang), "responsible-person-view"));

		list.add(common_outline);
		list.add(specific_outline);

		setContent(new _XMLDocument(currentTag));
		setContent(list);
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {

	}
}
