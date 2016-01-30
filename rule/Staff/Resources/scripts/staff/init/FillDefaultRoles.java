package staff.init;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;
import staff.dao.RoleDAO;
import staff.model.Role;

/**
 * 
 * 
 * @author Kayra created 08-01-2016
 */

public class FillDefaultRoles extends InitialDataAdapter<Role, RoleDAO> {

	@Override
	public List<Role> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		List<Role> entities = new ArrayList<Role>();

		/* Common roles */
		Role entity = new Role();
		entity.setName("staff_admin");
		entity.setDescription("the role allow to manage in Staff module");
		entities.add(entity);

		entity = new Role();
		entity.setName("reference_admin");
		entity.setDescription("the role allow to manage in Reference module");
		entities.add(entity);

		/* ComProperty application specific roles */
		entity = new Role();
		entity.setName("data_loader");
		entity.setDescription("Ответственное лицо по загрузке данных");
		entities.add(entity);

		return entities;
	}

	@Override
	public Class<RoleDAO> getDAO() {
		return RoleDAO.class;
	}

}
