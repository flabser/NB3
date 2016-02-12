package kz.lof.webserver;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletResponse;

import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.server.Server;
import kz.lof.webserver.valve.Logging;
import kz.lof.webserver.valve.Secure;
import kz.lof.webserver.valve.Unsecure;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;

public class WebServer {
	private static Tomcat tomcat;
	private static Engine engine;
	private static final String defaultWelcomeList[] = { "index.html", "index.htm" };

	public void init(String defaultHostName) throws MalformedURLException, LifecycleException {
		Server.logger.verboseLogEntry("Init webserver ...");

		tomcat = new Tomcat();
		tomcat.setPort(Environment.httpPort);
		tomcat.setHostname(defaultHostName);
		tomcat.setBaseDir("webserver");
		engine = tomcat.getEngine();

		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);

		getSharedResources("/" + EnvConst.SHARED_RESOURCES_APP_NAME);
		initDefaultURL();

	}

	public Context getSharedResources(String URLPath) throws LifecycleException, MalformedURLException {
		String db = new File("webapps/" + EnvConst.SHARED_RESOURCES_APP_NAME).getAbsolutePath();
		Context sharedResContext = tomcat.addContext(URLPath, db);
		sharedResContext.setDisplayName(EnvConst.SHARED_RESOURCES_APP_NAME);

		Tomcat.addServlet(sharedResContext, "default", "org.apache.catalina.servlets.DefaultServlet");
		sharedResContext.addServletMapping("/", "default");

		sharedResContext.addMimeMapping("css", "text/css");
		sharedResContext.addMimeMapping("js", "text/javascript");

		return sharedResContext;
	}

	public Host addApplication(String siteName, String URLPath, String docBase) throws LifecycleException, MalformedURLException {
		Context context = null;
		String db = new File("webapps/" + docBase).getAbsolutePath();

		if (docBase.equalsIgnoreCase(EnvConst.ADMINISTRATOR_APP_NAME)) {
			context = tomcat.addContext(URLPath, db);
			for (int i = 0; i < defaultWelcomeList.length; i++) {
				context.addWelcomeFile(defaultWelcomeList[i]);
			}
			Tomcat.addServlet(context, "Provider", "kz.flabs.servlets.admin.AdminProvider");
			context.setDisplayName(EnvConst.ADMINISTRATOR_APP_NAME);
		} else {
			context = tomcat.addContext(URLPath, db);
			context.setDisplayName(URLPath.substring(1));
			context.addWelcomeFile("Provider");
			Tomcat.addServlet(context, "Provider", "kz.lof.webserver.servlet.Provider");
		}

		Tomcat.addServlet(context, "default", "org.apache.catalina.servlets.DefaultServlet");
		context.addServletMapping("/", "default");

		context.addServletMapping("/Provider", "Provider");

		/*
		 * FilterDef filter2definition = new FilterDef();
		 * filter2definition.setFilterName(AccessGuard.class.getSimpleName());
		 * filter2definition.setFilterClass(AccessGuard.class.getName());
		 * context.addFilterDef(filter2definition);
		 * 
		 * FilterMap filter2mapping = new FilterMap();
		 * filter2mapping.setFilterName(AccessGuard.class.getSimpleName());
		 * filter2mapping.addURLPattern("/*");
		 * context.addFilterMap(filter2mapping);
		 */

		Tomcat.addServlet(context, "Login", "kz.flabs.servlets.Login");
		context.addServletMapping("/Login", "Login");

		Tomcat.addServlet(context, "Logout", "kz.flabs.servlets.Logout");
		context.addServletMapping("/Logout", "Logout");

		// Wrapper w = Tomcat.addServlet(context, "PortalInit",
		// "kz.flabs.servlets.PortalInit");
		Wrapper w = Tomcat.addServlet(context, "PortalInit", "kz.lof.webserver.servlet.PortalInit");
		w.setLoadOnStartup(1);

		context.addServletMapping("/PortalInit", "PortalInit");

		// Tomcat.addServlet(context, "Uploader", "kz.flabs.servlets.Uploader");
		// context.addServletMapping("/Uploader", "Uploader");

		Tomcat.addServlet(context, "UploadFile", "kz.flabs.servlets.UploadFile");
		context.addServletMapping("/UploadFile", "UploadFile");

		Tomcat.addServlet(context, "Error", "kz.flabs.servlets.Error");
		context.addServletMapping("/Error", "Error");

		context.addMimeMapping("css", "text/css");
		context.addMimeMapping("js", "text/javascript");

		return null;
	}

	public void initDefaultURL() {
		String db = new File(Environment.primaryAppDir + "webapps/ROOT").getAbsolutePath();
		Context context = tomcat.addContext(tomcat.getHost(), "", db);
		context.setDisplayName("root");

		engine.getPipeline().addValve(new Logging());
		engine.getPipeline().addValve(new Unsecure());
		engine.getPipeline().addValve(new Secure());

		initErrorPages(context);

		Tomcat.addServlet(context, "default", "org.apache.catalina.servlets.DefaultServlet");
		context.addServletMapping("/", "default");
	}

	public String initConnectors() {
		String portInfo = "";
		if (Environment.isSSLEnable) {
			Connector secureConnector = null;
			Server.logger.normalLogEntry("TLS connector has been enabled");
			secureConnector = tomcat.getConnector();
			// secureConnector.setDomain("flabs.kz");
			secureConnector.setPort(Environment.secureHttpPort);
			secureConnector.setScheme("https");
			secureConnector.setProtocol("org.apache.coyote.http11.Http11Protocol");
			secureConnector.setSecure(true);
			secureConnector.setEnableLookups(false);
			secureConnector.setSecure(true);
			secureConnector.setProperty("SSLEnabled", "true");
			secureConnector.setProperty("sslProtocol", "TLS");
			secureConnector.setProperty("keystoreFile", Environment.keyStore);
			secureConnector.setProperty("keystorePass", Environment.keyPwd);
			if (Environment.isClientSSLAuthEnable) {
				secureConnector.setProperty("clientAuth", "true");
				secureConnector.setProperty("truststoreFile", Environment.trustStore);
				secureConnector.setProperty("truststorePass", Environment.trustStorePwd);
			}
			tomcat.setConnector(secureConnector);
			portInfo = "secure:" + tomcat.getHost().getName() + ":" + Integer.toString(Environment.secureHttpPort);
		} else {
			portInfo = tomcat.getHost().getName() + ":" + Integer.toString(Environment.httpPort);
		}
		return portInfo;

	}

	public void startContainer() {
		try {
			tomcat.start();
			// tomcat.getServer().await();
		} catch (LifecycleException e) {
			Server.logger.errorLogEntry(e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopContainer();
			}
		});
	}

	public synchronized void stopContainer() {
		try {
			if (tomcat != null) {
				tomcat.stop();
			}
		} catch (LifecycleException exception) {
			Server.logger.errorLogEntry("Cannot Stop WebServer" + exception.getMessage());
		}

	}

	private void initErrorPages(Context context) {
		ErrorPage er = new ErrorPage();
		er.setErrorCode(HttpServletResponse.SC_NOT_FOUND);
		er.setLocation("/Error");
		context.addErrorPage(er);
		ErrorPage er401 = new ErrorPage();
		er401.setErrorCode(HttpServletResponse.SC_UNAUTHORIZED);
		er401.setLocation("/Error");
		context.addErrorPage(er401);
		ErrorPage er400 = new ErrorPage();
		er400.setErrorCode(HttpServletResponse.SC_BAD_REQUEST);
		er400.setLocation("/Error");
		context.addErrorPage(er);
		ErrorPage er500 = new ErrorPage();
		er500.setErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		er500.setLocation("/Error");
		context.addErrorPage(er);
	}

}
