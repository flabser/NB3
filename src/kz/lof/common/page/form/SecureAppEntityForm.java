package kz.lof.common.page.form;

import java.util.UUID;

import kz.lof.common.dao.SecureEntityDAO;
import kz.lof.dataengine.jpa.IAppEntity;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;

public class SecureAppEntityForm extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		IAppEntity entity;
		String id = formData.getValueSilently("docid");
		if (!id.isEmpty()) {
			SecureEntityDAO dao = new SecureEntityDAO(session);
			entity = dao.findById(UUID.fromString(id));
			// addContent(entity);
		}

		_ActionBar actionBar = new _ActionBar(session);
		actionBar.addAction(new _Action(getLocalizedWord("close", session.getLang()), "", _ActionType.CLOSE));
		addContent(actionBar);
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData) {

	}

}
