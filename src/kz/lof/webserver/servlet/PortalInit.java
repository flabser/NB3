package kz.lof.webserver.servlet;

import java.lang.reflect.Field;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpadatabase.ftengine.FTEntity;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.server.Server;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PortalInit extends HttpServlet {

	private static final long serialVersionUID = -8913620140247217298L;
	private boolean isValid;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		String app = context.getServletContextName();
		AppEnv env = new AppEnv(app);

		try {
			IDatabaseDeployer dd = new kz.lof.dataengine.jpadatabase.DatabaseDeployer(env);
			IDatabase db = new kz.lof.dataengine.jpadatabase.Database(env);
			dd.deploy();
			env.setDataBase(db);

			try {
				Class c = Class.forName(env.appName.toLowerCase() + ".init.AppConst");
				ObjectMapper mapper = new ObjectMapper();
				String result = "";
				Field f = c.getDeclaredField("FT_INDEX_SCOPE");
				f.setAccessible(true);
				if (f.isAccessible()) {
					result = (String) f.get(null);
				}
				FTEntity fe = mapper.readValue(result, FTEntity.class);
				IFTIndexEngine ftEngine = db.getFTSearchEngine();
				ftEngine.registerTable(fe);
			} catch (ClassNotFoundException e) {

			}
			isValid = true;

		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}

		if (isValid) {
			Environment.addApplication(env);
		}

		if (isValid) {
			context.setAttribute(EnvConst.APP_ATTR, env);
		}

	}

}
