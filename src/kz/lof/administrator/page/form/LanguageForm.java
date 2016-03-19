package kz.lof.administrator.page.form;

import java.util.UUID;

import kz.lof.administrator.dao.LanguageDAO;
import kz.lof.administrator.model.Language;
import kz.lof.exception.SecureException;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;
import kz.lof.scripting._Validation;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;

import org.eclipse.persistence.exceptions.DatabaseException;

public class LanguageForm extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		LanguageCode lang = session.getLang();
		String id = formData.getValueSilently("docid");
		Language entity;
		if (!id.isEmpty()) {
			LanguageDAO dao = new LanguageDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Language();
		}
		addContent(entity);
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
		LanguageDAO dao = new LanguageDAO(session);
		Language entity;

		if (id.isEmpty()) {
			isNew = true;
			entity = new Language();
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
