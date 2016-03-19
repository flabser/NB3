package kz.lof.administrator.page.view;

import kz.lof.administrator.dao.UserDAO;
import kz.lof.administrator.model.User;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;

/**
 * @author Kayra created 04-01-2016
 */

public class UserView extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		_ActionBar actionBar = new _ActionBar(session);
		_Action newDocAction = new _Action(getLocalizedWord("new_", session.getLang()), "", "new_user");
		newDocAction.setURL("Provider?id=user-form");
		actionBar.addAction(newDocAction);
		actionBar.addAction(new _Action(getLocalizedWord("del_document", session.getLang()), "", _ActionType.DELETE_DOCUMENT));

		UserDAO dao = new UserDAO();
		int pageNum = formData.getNumberValueSilently("page", 1);
		int pageSize = session.pageSize;
		String keyword = formData.getValueSilently("keyword");
		addContent(actionBar);
		ViewPage<User> vp = dao.findAll(keyword, pageNum, pageSize);
		addContent(new _POJOListWrapper(vp.getResult(), vp.getMaxPage(), vp.getCount(), vp.getPageNum(), session));
	}

	@Override
	public void doDELETE(_Session session, _WebFormData formData) {
		UserDAO dao = new UserDAO();
		for (String id : formData.getListOfValuesSilently("docid")) {
			User m = dao.findById(Long.parseLong(id));
			dao.delete(m);
		}
	}
}
