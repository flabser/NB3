package kz.lof.administrator.page.view;

import java.util.List;

import kz.lof.administrator.dao.LanguageDAO;
import kz.lof.scripting.IPOJOObject;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;

public class LanguageView extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		_ActionBar actionBar = new _ActionBar(session);
		_Action newDocAction = new _Action(getLocalizedWord("new_", session.getLang()), "", "new_language");
		newDocAction.setURL("Provider?id=language-form");
		actionBar.addAction(newDocAction);
		actionBar.addAction(new _Action(getLocalizedWord("del_document", session.getLang()), "", _ActionType.DELETE_DOCUMENT));

		LanguageDAO dao = new LanguageDAO(session);
		addContent(actionBar);
		List<? extends IPOJOObject> list = dao.findAll();
		addContent(new _POJOListWrapper(list, 0, dao.getCount(), 0, session));
	}
}
