package reference.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._EnumWrapper;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import reference.dao.CountryDAO;
import reference.model.Country;
import reference.model.constants.CountryCode;

/**
 * @author Kayra created 03-01-2016
 */

public class CountryForm extends ReferenceForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		Country entity;
		if (!id.equals("")) {
			CountryDAO dao = new CountryDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Country();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
		setContent(new _EnumWrapper<>(CountryCode.class.getEnumConstants()));
		setContent(getSimpleActionBar(session, lang));
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {
		try {
			boolean v = validate(formData, lang);
			if (v == false) {
				setBadRequest();
				return;
			}

			boolean isNew = false;
			String id = formData.getValueSilently("docid");
			CountryDAO dao = new CountryDAO(session);
			Country entity;

			if (id.equals("")) {
				isNew = true;
				entity = new Country();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new Country();
				}
			}

			entity.setName(formData.getValue("name"));
			entity.setCode(CountryCode.valueOf(formData.getValueSilently("code", "UNKNOWN")));

			if (isNew) {
				dao.add(entity);
			} else {
				dao.update(entity);
			}
			addMsg(getLocalizedWord("document_was_saved_succesfully", lang));
		} catch (_Exception e) {
			log(e);
		}
	}
}
