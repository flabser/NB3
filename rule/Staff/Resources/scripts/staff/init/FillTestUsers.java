package staff.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.nextbase.script._Session;
import kz.pchelka.env.EnvConst;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;
import staff.dao.EmployeeDAO;
import staff.dao.OrganizationDAO;
import staff.dao.RoleDAO;
import staff.exception.EmployеeException;
import staff.model.Employee;
import staff.model.Organization;
import staff.model.Role;

/**
 * 
 * 
 * @author Kayra created 24-01-2016
 */

public class FillTestUsers extends InitialDataAdapter<Employee, EmployeeDAO> {
	private _Session ses;
	private static String file1 = EnvConst.RESOURCES_DIR + File.separator + "Roman.txt";
	private static String file2 = EnvConst.RESOURCES_DIR + File.separator + "Fantasy.txt";

	@Override
	public List<Employee> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {
		this.ses = ses;
		List<Employee> entities = new ArrayList<Employee>();
		if (checkNecessaryFiles()) {
			ISystemDatabase sysDb = Environment.systemBase;
			List<User> users = sysDb.getAllUsers("", 0, 10000);
			int rCount = users.size();
			System.out.println("System users count = " + rCount);
			for (int i = 0; i < rCount; i++) {
				entities.add(getMock(users.get(i)));
			}
		} else {
			System.out.println("There is no \"" + file1 + "\" or \"" + file2 + "\" file");
		}
		return entities;
	}

	@Override
	public Class<EmployeeDAO> getDAO() {
		return EmployeeDAO.class;
	}

	private Employee getMock(User user) {
		Employee c = new Employee();
		c.setName(getRndFirstName() + " " + getRndLastName());
		try {
			c.setLogin(user.getUserID());
		} catch (EmployеeException e) {
			e.printStackTrace();
		}
		Organization o = new OrganizationDAO(ses).findPrimaryOrg();
		c.setOrganization(o);
		RoleDAO roleDao = new RoleDAO(ses);
		List<Role> rl = roleDao.findAll();
		Role role = (Role) Util.getRndListElement(rl);
		if (role != null) {
			c.addRole(role);
		}

		return c;

	}

	private String getRndFirstName() {
		try {
			NameGenerator n = new NameGenerator(file1);
			return n.compose(3);
		} catch (IOException e) {
			Server.logger.errorLogEntry(e);
		}
		return "";
	}

	private String getRndLastName() {
		try {
			NameGenerator n = new NameGenerator(file2);
			return n.compose(3);
		} catch (IOException e) {
			Server.logger.errorLogEntry(e);
		}
		return "";
	}

	private boolean checkNecessaryFiles() {
		File f1 = new File(file1);
		File f2 = new File(file2);
		if (f1.exists() && f2.exists()) {
			return true;
		}
		return false;

	}
}
