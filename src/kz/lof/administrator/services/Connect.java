package kz.lof.administrator.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import kz.lof.administrator.dao.UserDAO;
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

public class Connect {
	private AppEnv env = Environment.getAppEnv(EnvConst.ADMINISTRATOR_APP_NAME);

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
