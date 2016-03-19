package kz.lof.administrator.services;

import java.util.ArrayList;
import java.util.List;

import kz.flabs.dataengine.ISystemDatabase;
import kz.lof.administrator.dao.ApplicationDAO;
import kz.lof.administrator.dao.UserDAO;
import kz.lof.administrator.model.Application;
import kz.lof.administrator.model.User;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.user.AnonymousUser;
import kz.lof.user.IUser;

public class UserServices {
	AppEnv env = Environment.getAppEnv(EnvConst.ADMINISTRATOR_APP_NAME);

	@Deprecated
	public void importFromH2() {
		importFromH2(true);
	}

	@SuppressWarnings("deprecation")
	public void importFromH2(boolean showConsoleOutput) {
		List<User> entities = new ArrayList<User>();

		ISystemDatabase sysDb = Environment.systemBase;
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

	}

	public IUser<Long> getUser(String login) {
		UserDAO uDao = new UserDAO();
		IUser<Long> user = uDao.findByLogin(login);
		return user;

	}
}
