package administrator.page.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import administrator.dao.ApplicationDAO;
import administrator.dao.UserDAO;
import administrator.model.Application;
import administrator.model.User;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._Session;
import kz.lof.scripting._Validation;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;
import kz.nextbase.script._Exception;
import kz.nextbase.script._Validator;
import kz.nextbase.script.actions._Action;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.actions._ActionType;

/**
 * @author Kayra created 05-03-2016
 */

public class UserForm extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		LanguageCode lang = session.getLang();
		String id = formData.getValueSilently("docid");
		User entity;
		if (!id.isEmpty()) {
			UserDAO dao = new UserDAO(session);
			entity = dao.findById(Long.parseLong(id));
		} else {
			entity = new User();
			entity.setRegDate(new Date());
			entity.setLogin("");
		}
		addContent(entity);
		addContent(new _POJOListWrapper(new ApplicationDAO(session).findAll(), session));
		_ActionBar actionBar = new _ActionBar(session);
		actionBar.addAction(new _Action(getLocalizedWord("save_close", lang), "", _ActionType.SAVE_AND_CLOSE));
		actionBar.addAction(new _Action(getLocalizedWord("close", lang), "", _ActionType.CLOSE));
		addContent(actionBar);
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData) {
		try {
			_Validation ve = validate(formData, session.getLang());
			if (ve.hasError()) {
				setBadRequest();
				setValidation(ve);
				return;
			}

			int id = formData.getNumberValueSilently("docid", -1);
			UserDAO dao = new UserDAO(session);
			User entity;

			boolean isNew = id == -1;
			if (isNew) {
				entity = new User();
			} else {
				entity = dao.findById(id);
			}

			entity.setLogin(formData.getValue("login"));
			entity.setEmail(formData.getValue("email"));
			entity.setPwd(formData.getValue("pwd"));
			List<Application> apps = new ArrayList<Application>();
			ApplicationDAO aDao = new ApplicationDAO(session);
			for (String appId : formData.getListOfValuesSilently("app")) {
				if (!appId.isEmpty()) {
					Application application = aDao.findById(appId);
					if (application != null) {
						apps.add(application);
					}
				}
			}
			entity.setAllowedApps(apps);

			if (isNew) {
				dao.add(entity);
			} else {
				dao.update(entity);
			}

			setRedirect("Provider?id=user-view");
		} catch (_Exception e) {
			error(e);
		}
	}

	private _Validation validate(_WebFormData formData, LanguageCode lang) {
		_Validation ve = new _Validation();

		if (formData.getValueSilently("login").isEmpty()) {
			ve.addError("login", "required", getLocalizedWord("required", lang));
		}
		if (formData.getValueSilently("email").isEmpty() || !_Validator.checkEmail(formData.getValueSilently("email"))) {
			ve.addError("email", "email", getLocalizedWord("email_invalid", lang));
		}
		if (!formData.getValueSilently("pwd").isEmpty()) {
			if (formData.getValueSilently("pwd_confirm").isEmpty()) {
				ve.addError("pwd_confirm", "required", getLocalizedWord("required", lang));
			} else if (!formData.getValueSilently("pwd").equals(formData.getValueSilently("pwd_confirm"))) {
				ve.addError("pwd_confirm", "required", getLocalizedWord("password_confirm_not_equals", lang));
			}
		}

		return ve;
	}
}
