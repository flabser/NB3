package staff.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._EnumWrapper;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import staff.dao.DepartmentDAO;
import staff.dao.RoleDAO;
import staff.model.Department;
import staff.model.constants.DepartmentType;

/**
 * @author Kayra created 07-01-2016
 */

public class DepartmentForm extends StaffForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		Department entity;
		if (!id.equals("")) {
			DepartmentDAO dao = new DepartmentDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Department();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
		setContent(new _EnumWrapper<>(DepartmentType.class.getEnumConstants()));
		setContent(new _POJOListWrapper(new RoleDAO(session).findAll(), lang));
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
			DepartmentDAO dao = new DepartmentDAO(session);
			Department entity;

			if (id.equals("")) {
				isNew = true;
				entity = new Department();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new Department();
				}
			}

			entity.setName(formData.getValue("name"));
			entity.setType(DepartmentType.valueOf(formData.getValueSilently("type", "UNKNOWN")));

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
