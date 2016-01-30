package reference.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._EnumWrapper;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import reference.dao.CountryDAO;
import reference.dao.RegionDAO;
import reference.model.Country;
import reference.model.Region;
import reference.model.constants.RegionType;

/**
 * @author Kayra created 03-01-2016
 */

public class RegionForm extends ReferenceForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		Region entity;
		if (!id.equals("")) {
			RegionDAO dao = new RegionDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Region();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
		setContent(new _EnumWrapper<>(RegionType.class.getEnumConstants(), getLocalizedWord(RegionType.class.getEnumConstants(), lang.toString())));
		setContent(new _POJOListWrapper<>(new CountryDAO(session).findAll(), lang));
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
			RegionDAO dao = new RegionDAO(session);
			Region entity;

			if (id.equals("")) {
				isNew = true;
				entity = new Region();
			} else {
				entity = dao.findById(UUID.fromString(id));
			}

			entity.setName(formData.getValue("name"));
			entity.setType(RegionType.valueOf(formData.getValueSilently("region_type", "UNKNOWN")));
			CountryDAO countryDao = new CountryDAO(session);
			Country country = countryDao.findById(UUID.fromString(formData.getValueSilently("country_id")));
			entity.setCountry(country);

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

	@Override
	protected boolean validate(_WebFormData formData, LanguageType lang) {
		if (super.validate(formData, lang)) {
			return false;
		} else if (formData.getValueSilently("region_type").isEmpty() || formData.getValueSilently("region_type").equals("UNKNOWN")) {
			addMsg("field_region_type_is_empty");
			return false;
		} else if (formData.getValueSilently("country_id").isEmpty()) {
			addMsg("field_country_type_is_empty");
			return false;
		}

		return true;
	}
}
