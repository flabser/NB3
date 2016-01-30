package staff.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import staff.dao.RoleDAO;
import staff.model.Role;

/**
 * @author Kayra created 10-01-2016
 */

public class RoleForm extends StaffForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		Role entity;
		if (!id.equals("")) {
			RoleDAO dao = new RoleDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Role();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
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
			RoleDAO dao = new RoleDAO(session);
			Role entity;

			if (id.equals("")) {
				isNew = true;
				entity = new Role();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new Role();
				}
			}

			entity.setName(formData.getValue("name"));
			entity.setDescription(formData.getValue("description"));

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
