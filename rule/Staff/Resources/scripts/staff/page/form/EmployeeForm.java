package staff.page.form;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import staff.dao.DepartmentDAO;
import staff.dao.EmployeeDAO;
import staff.dao.OrganizationDAO;
import staff.dao.RoleDAO;
import staff.exception.EmployеeException;
import staff.model.Employee;
import staff.model.Role;

/**
 * @author Kayra created 07-01-2016
 */

public class EmployeeForm extends StaffForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		Employee entity;
		if (!id.equals("")) {
			EmployeeDAO dao = new EmployeeDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new Employee();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
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
			OrganizationDAO orgDAO = new OrganizationDAO(session);
			DepartmentDAO depDAO = new DepartmentDAO(session);
			RoleDAO roleDAO = new RoleDAO(session);
			EmployeeDAO dao = new EmployeeDAO(session);
			Employee entity;

			if (id.equals("")) {
				isNew = true;
				entity = new Employee();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new Employee();
				}
			}

			entity.setName(formData.getValue("name"));
			entity.setLogin(formData.getValueSilently("login"));
			entity.setOrganization(orgDAO.findById(UUID.fromString(formData.getValue("organization_id"))));
			entity.setDepartment(depDAO.findById(UUID.fromString(formData.getValue("department_id"))));

			String[] roles = formData.getListOfValuesSilently("role");
			List<Role> roleList = new ArrayList<>();
			for (String roleId : roles) {
				Role role = roleDAO.findById(UUID.fromString(roleId));
				roleList.add(role);
			}
			if (!roleList.isEmpty()) {
				entity.setRoles(roleList);
			}

			if (isNew) {
				dao.add(entity);
			} else {
				dao.update(entity);
			}

			addMsg(getLocalizedWord("document_was_saved_succesfully", lang));
		} catch (_Exception e) {
			setBadRequest();
			log(e);
		} catch (EmployеeException e) {
			setBadRequest();
			log(e);
		}
	}
}
