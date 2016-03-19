package kz.lof.administrator.page.form;

import java.util.Date;
import java.util.UUID;

import kz.lof.administrator.dao.ApplicationDAO;
import kz.lof.administrator.model.Application;
import kz.lof.dataengine.jpa.constants.AppCode;
import kz.lof.exception.SecureException;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.scripting._Validation;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script._EnumWrapper;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;

import org.eclipse.persistence.exceptions.DatabaseException;

public class ApplicationForm extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		LanguageCode lang = session.getLang();
		String id = formData.getValueSilently("docid");
		Application entity;
		if (!id.isEmpty()) {
			ApplicationDAO dao = new ApplicationDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Application();
			entity.setRegDate(new Date());
			entity.setName("");
		}
		addContent(entity);
		addContent(new _EnumWrapper<>(AppCode.class.getEnumConstants()));
		_ActionBar actionBar = new _ActionBar(session);
		actionBar.addAction(new _Action(getLocalizedWord("save_close", lang), "", _ActionType.SAVE_AND_CLOSE));
		actionBar.addAction(new _Action(getLocalizedWord("close", lang), "", _ActionType.CLOSE));
		addContent(actionBar);
		startSaveFormTransact(entity);
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData) {
		_Validation ve = validate(formData, session.getLang());
		if (ve.hasError()) {
			setBadRequest();
			setValidation(ve);
			return;
		}

		boolean isNew = false;
		String id = formData.getValueSilently("docid");
		ApplicationDAO dao = new ApplicationDAO(session);
		Application entity;

		if (id.isEmpty()) {
			isNew = true;
			entity = new Application();
		} else {
			entity = dao.findById(UUID.fromString(id));
		}

		try {
			if (isNew) {
				dao.add(entity);
			} else {
				dao.update(entity);
			}
		} catch (DatabaseException | SecureException e) {
			setError(e);
		}
	}

	protected _Validation validate(_WebFormData formData, LanguageCode lang) {
		_Validation ve = new _Validation();

		if (formData.getValueSilently("name").isEmpty()) {
			ve.addError("name", "required", getLocalizedWord("required", lang));
		}

		return ve;
	}
}
