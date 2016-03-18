package kz.lof.webserver.servlet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpadatabase.ftengine.FTEntity;
import kz.lof.dataengine.system.IEmployeeDAO;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.AnonymousUser;

public class PortalInit extends HttpServlet {

	private static final long serialVersionUID = -8913620140247217298L;
	private boolean isValid;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		String app = context.getServletContextName();
		String global = Environment.webAppToStart.get(app).global;
		AppEnv env = new AppEnv(app, global);

		try {
			IDatabaseDeployer dd = new kz.lof.dataengine.jpadatabase.DatabaseDeployer(env);
			IDatabase db = new kz.lof.dataengine.jpadatabase.Database(env);
			dd.deploy();
			env.setDataBase(db);

			if (env.appName.equalsIgnoreCase(EnvConst.STAFF_APP_NAME)) {
				Class<?> clazz = Class.forName(EnvConst.STAFF_DAO_CLASS);
				Class[] args = new Class[] { _Session.class };
				Constructor<?> contructor = clazz.getConstructor(args);
				_Session ses = new _Session(env, new AnonymousUser());
				IEmployeeDAO aDao = (IEmployeeDAO) contructor.newInstance(new Object[] { ses });
				Environment.systemBase.setEmployeeDAO(aDao);
				AppEnv.logger.debugLogEntry("Module \"" + env.appName + "\" has been connected to system");
			}

			// TODO it need to improve
			IFTIndexEngine ftEngine = db.getFTSearchEngine();
			if (env.appName.equalsIgnoreCase("municipalproperty")) {
				List<String> fields = new ArrayList<String>();
				fields.add("object_name");
				fields.add("description");
				fields.add("notes");
				fields.add("inv_number");
//				fields.add("balanceholder");
				ftEngine.registerTable(new FTEntity("properties", fields, "municipalproperty.dao.PropertyDAO"));
			}

			isValid = true;

		} catch (Exception e) {
			if (e instanceof DatabasePoolException) {
				Server.logger.fatalLogEntry("Application \"" + env.appName + "\" has not connected to database ");
				Environment.reduceApplication();
			} else {
				Server.logger.errorLogEntry(e);
				Environment.reduceApplication();
			}
		}

		if (isValid) {
			Environment.addApplication(env);
		}

		if (isValid) {
			context.setAttribute(EnvConst.APP_ATTR, env);
		}

	}

}
