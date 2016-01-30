package staff.dao;

import java.util.UUID;

import kz.flabs.dataengine.jpa.DAO;
import kz.nextbase.script._Session;
import staff.model.Department;

public class DepartmentDAO extends DAO<Department, UUID> {

	public DepartmentDAO(_Session session) {
		super(Department.class, session);
	}

}
