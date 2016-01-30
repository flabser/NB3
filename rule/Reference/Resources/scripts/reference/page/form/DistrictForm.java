package reference.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import reference.dao.DistrictDAO;
import reference.dao.RegionDAO;
import reference.model.District;

/**
 * @author Kayra created 03-01-2016
 */

public class DistrictForm extends ReferenceForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		District entity;
		if (!id.equals("")) {
			DistrictDAO dao = new DistrictDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new District();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
		setContent(getSimpleActionBar(session, lang));
	}

	@Override
	public void doPOST(_Session session, _WebFormData webFormData, LanguageType lang) {
		try {
			boolean v = validate(webFormData, lang);
			if (v == false) {
				setBadRequest();
				return;
			}

			boolean isNew = false;
			String id = webFormData.getValueSilently("docid");
			DistrictDAO dao = new DistrictDAO(session);
			RegionDAO regionDAO = new RegionDAO(session);
			District entity;

			if (id.equals("")) {
				isNew = true;
				entity = new District();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new District();
				}
			}

			entity.setName(webFormData.getValue("name"));
			entity.setRegion(regionDAO.findById(UUID.fromString(webFormData.getValue("region"))));

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
