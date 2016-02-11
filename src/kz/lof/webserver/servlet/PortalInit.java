package kz.lof.webserver.servlet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import kz.flabs.appdaemon.AppDaemonRule;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.runtimeobj.Application;
import kz.flabs.users.User;
import kz.lof.dataengine.jpadatabase.ftengine.FTEntity;
import kz.lof.dataengine.system.IEmployeeDAO;
import kz.lof.env.EnvConst;
import kz.lof.server.Server;
import kz.nextbase.script._Session;
import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.IDaemon;

public class PortalInit extends HttpServlet {

	private static final long serialVersionUID = -8913620140247217298L;
	private boolean isValid;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		String app = context.getServletContextName();
		String global = Environment.webAppToStart.get(app).global;
		AppEnv env = new AppEnv(app, global);
		if (env.globalSetting.databaseEnable) {
			IDatabaseDeployer dd = null;
			try {
				@SuppressWarnings("rawtypes")
				Class[] intArgsClass = new Class[] { AppEnv.class };
				Constructor<?> deployerConstr = env.globalSetting.dbImpl.getDeployerClass().getConstructor(intArgsClass);
				dd = (IDatabaseDeployer) deployerConstr.newInstance(new Object[] { env });

				Constructor<?> dbConstr = env.globalSetting.dbImpl.getDatabaseClass().getConstructor(intArgsClass);
				IDatabase db = (IDatabase) dbConstr.newInstance(new Object[] { env });

				if (env.globalSetting.autoDeployEnable) {
					Server.logger.normalLogEntry("Checking database structure ...");
					dd.deploy();
				}
				AppEnv.logger.verboseLogEntry("Application will use \"" + db + "\" database");
				env.setDataBase(db);

				if (env.appType.equalsIgnoreCase(EnvConst.STAFF_APP_NAME)) {
					Class<?> clazz = Class.forName(EnvConst.STAFF_DAO_CLASS);
					Class[] args = new Class[] { _Session.class };
					Constructor<?> contructor = clazz.getConstructor(args);
					_Session ses = new _Session(env, new User(env));
					IEmployeeDAO aDao = (IEmployeeDAO) contructor.newInstance(new Object[] { ses });
					Environment.systemBase.setEmployeeDAO(aDao);
					AppEnv.logger.verboseLogEntry("Module \"" + env.appType + "\" has been connected to system");
				}

				// TODO it need to improve
				IFTIndexEngine ftEngine = db.getFTSearchEngine();
				if (env.appType.equalsIgnoreCase("municipalproperty")) {
					List<String> fields = new ArrayList<String>();
					fields.add("object_name");
					fields.add("description");
					fields.add("notes");
					fields.add("inv_number");
					ftEngine.registerTable(new FTEntity("properties", fields, "municipalproperty.dao.PropertyDAO"));
				}

				isValid = true;

			} catch (Exception e) {
				if (e instanceof DatabasePoolException) {
					Server.logger.fatalLogEntry("Application \"" + env.appType + "\" has not connected to database " + env.globalSetting.databaseType
					        + "(" + env.globalSetting.dbURL + ")");
					Environment.reduceApplication();
				} else {
					Server.logger.errorLogEntry(e);
					Environment.reduceApplication();
				}
			}

		} else {

			isValid = true;
		}

		if (isValid) {

			Environment.addApplication(env);

			if (env.globalSetting.databaseEnable) {
				for (AppDaemonRule rule : env.globalSetting.schedSettings) {
					try {
						Class<?> c = Class.forName(rule.getClassName());
						IDaemon daemon = (IDaemon) c.newInstance();
						daemon.init(rule);
					} catch (InstantiationException e) {
						Server.logger.errorLogEntry(e);
					} catch (IllegalAccessException e) {
						Server.logger.errorLogEntry(e);
					} catch (ClassNotFoundException e) {
						Server.logger.errorLogEntry(e);
					}
				}

			}
			env.application = new Application(env);
		}

		if (isValid) {
			context.setAttribute(EnvConst.APP_ATTR, env);
		}

	}

}
