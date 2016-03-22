package kz.lof.administrator.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.ISystemDatabase;
import kz.lof.administrator.dao.ApplicationDAO;
import kz.lof.administrator.dao.UserDAO;
import kz.lof.administrator.model.Application;
import kz.lof.administrator.model.User;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.system.IEmployee;
import kz.lof.dataengine.system.IEmployeeDAO;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.AnonymousUser;
import kz.lof.user.IUser;
import kz.lof.util.StringUtil;

public class UserServices {
	private AppEnv env = Environment.getAppEnv(EnvConst.ADMINISTRATOR_APP_NAME);

	@Deprecated
	public void importFromH2() {
		importFromH2(true);
	}

	@SuppressWarnings("deprecation")
	public void importFromH2(boolean showConsoleOutput) {
		List<User> entities = new ArrayList<User>();

		ISystemDatabase sysDb = null;
		try {
			sysDb = new kz.flabs.dataengine.h2.SystemDatabase();

			List<kz.flabs.users.User> users = sysDb.getAllUsers("", 0, 10000);
			int rCount = users.size();
			if (showConsoleOutput) {
				System.out.println("System users count = " + rCount);
			}
			ApplicationDAO aDao = new ApplicationDAO(new _Session(env, new AnonymousUser()));
			List<Application> appList = new ArrayList<Application>();
			appList.add(aDao.findByName("MunicipalProperty"));
			appList.add(aDao.findByName("Accountant"));
			appList.add(aDao.findByName("PropertyLeasing"));
			appList.add(aDao.findByName("Registry"));

			for (kz.flabs.users.User oldUser : users) {
				User entity = new User();
				entity.setLogin(oldUser.getLogin());
				entity.setPwd(oldUser.getPassword());
				entity.setPwdHash(oldUser.getPasswordHash());
				entity.setAllowedApps(appList);
				entities.add(entity);
			}

			UserDAO uDao = new UserDAO();
			for (User user : entities) {
				uDao.add(user);
			}

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | DatabasePoolException e) {
			Server.logger.errorLogEntry(e);
		}

	}

	public IUser<Long> getUser(String login, String pwd) {

		UserDAO uDao = new UserDAO();
		IUser<Long> user = uDao.findByLogin(login);

		if (user != null) {
			String pwdHash = StringUtil.encode(pwd);
			if (user.getPwdHash() != null && user.getPwdHash().equals(pwdHash)) {
				user.setAuthorized(true);
			} else {
				Server.logger.errorLogEntry("password has not been encoded");
			}

			if (user.isAuthorized()) {
				IEmployeeDAO eDao = null;
				try {
					Class<?> clazz = Class.forName(EnvConst.STAFF_DAO_CLASS);
					Class[] args = new Class[] { _Session.class };
					Constructor<?> contructor = clazz.getConstructor(args);
					_Session ses = new _Session(env, new AnonymousUser());
					eDao = (IEmployeeDAO) contructor.newInstance(new Object[] { ses });
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				        | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
					Server.logger.errorLogEntry(e);
				}

				if (eDao != null) {
					IEmployee emp = eDao.getEmployee(user.getId());
					if (emp != null) {
						user.setUserName(emp.getName());
					} else {
						user.setUserName(user.getLogin());
					}
				}
			}

		} else {
			Server.logger.warningLogEntry("\"" + login + "\" user not found");
		}

		return user;

	}
}
