package reference.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._URL;
import kz.nextbase.script._WebFormData;
import reference.dao.BuildingMaterialDAO;
import reference.model.BuildingMaterial;

public class BuildingMaterialForm extends ReferenceForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		BuildingMaterial entity;
		if (!id.equals("")) {
			BuildingMaterialDAO dao = new BuildingMaterialDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new BuildingMaterial();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
		setContent(getSimpleActionBar(session, lang));
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {
		try {
			boolean v = validate(formData, lang);
			if (!v) {
				setBadRequest();
				return;
			}

			boolean isNew = false;
			String id = formData.getValueSilently("docid");
			BuildingMaterialDAO dao = new BuildingMaterialDAO(session);
			BuildingMaterial entity;

			if (id.equals("")) {
				isNew = true;
				entity = new BuildingMaterial();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new BuildingMaterial();
				}
			}

			entity.setName(formData.getValue("name"));

			if (isNew) {
				dao.add(entity);
			} else {
				dao.update(entity);
			}

			_URL returnURL = session.getURLOfLastPage();
			localizedMsgBox(getLocalizedWord("document_was_saved_succesfully", lang));
			setRedirectURL(returnURL);
		} catch (_Exception e) {
			log(e);
		}
	}
}
