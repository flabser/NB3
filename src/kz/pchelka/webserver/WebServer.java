package kz.pchelka.webserver;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import kz.pchelka.env.AuthTypes;
import kz.pchelka.env.Environment;
import kz.pchelka.env.Site;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.realm.LockOutRealm;
import org.apache.catalina.startup.Tomcat;

public class WebServer implements IWebServer {
	private Tomcat tomcat;
	private static final String defaultWelcomeList[] = { "index.html", "index.htm" };

	@Override
	public void init(String defaultHostName) throws MalformedURLException, LifecycleException {
		kz.pchelka.server.Server.logger.verboseLogEntry("Init webserver ...");

		tomcat = new Tomcat();
		tomcat.setPort(Environment.httpPort);
		tomcat.setHostname(defaultHostName);
		tomcat.setBaseDir("webserver");

		StandardServer server = (StandardServer) this.tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);

		getSharedResources("/SharedResources");
		initDefaultURL();
		try {
			if (Environment.webServicesEnable) {
				initWebServices();
			}
		} catch (ServletException e) {
			kz.pchelka.server.Server.logger.errorLogEntry(e);
		}

	}

	public Context getSharedResources(String URLPath) throws LifecycleException, MalformedURLException {
		String db = new File("webapps/SharedResources").getAbsolutePath();
		Context sharedResContext = tomcat.addContext(URLPath, db);
		sharedResContext.setDisplayName("sharedresources");

		Tomcat.addServlet(sharedResContext, "default", "org.apache.catalina.servlets.DefaultServlet");
		sharedResContext.addServletMapping("/", "default");

		sharedResContext.addMimeMapping("css", "text/css");
		sharedResContext.addMimeMapping("js", "text/javascript");

		return sharedResContext;
	}

	@Override
	public Host addApplication(String siteName, String URLPath, String docBase) throws LifecycleException, MalformedURLException {
		Context context = null;

		if (docBase.equalsIgnoreCase("Administrator")) {
			String db = new File("webapps/" + docBase).getAbsolutePath();
			context = tomcat.addContext(URLPath, db);
			for (int i = 0; i < defaultWelcomeList.length; i++) {
				context.addWelcomeFile(defaultWelcomeList[i]);
			}
			Tomcat.addServlet(context, "Provider", "kz.flabs.servlets.admin.AdminProvider");
			context.setDisplayName("Administrator");
		} else {
			if (siteName == null || siteName.equalsIgnoreCase("")) {
				String db = new File("webapps/" + docBase).getAbsolutePath();
				context = tomcat.addContext(URLPath, db);
				context.setDisplayName(URLPath.substring(1));
			} else {
				URLPath = "";
				StandardHost appHost = new StandardHost();
				appHost.setName(siteName);
				String baseDir = new File("webapps/" + docBase).getAbsolutePath();
				appHost.setAppBase(baseDir);

				context = tomcat.addContext(appHost, URLPath, baseDir);
				context.setDisplayName(siteName);
			}
			context.addWelcomeFile("Provider");
			Tomcat.addServlet(context, "Provider", "kz.flabs.servlets.Provider");
		}

		Tomcat.addServlet(context, "default", "org.apache.catalina.servlets.DefaultServlet");
		context.addServletMapping("/", "default");

		context.addServletMapping("/Provider", "Provider");

		FilterDef filterAccessGuard = new FilterDef();
		filterAccessGuard.setFilterName("AccessGuard");
		filterAccessGuard.setFilterClass("kz.flabs.filters.AccessGuard");

		FilterMap filterAccessGuardMapping = new FilterMap();
		filterAccessGuardMapping.setFilterName("AccessGuard");
		filterAccessGuardMapping.addServletName("Provider");

		context.addFilterDef(filterAccessGuard);
		context.addFilterMap(filterAccessGuardMapping);

		Site webApp = Environment.webAppToStart.get(docBase);
		if (webApp != null && webApp.authType == AuthTypes.BASIC) {

			LockOutRealm lr = new LockOutRealm();

			JDBCRealm jdbcr = new JDBCRealm();
			jdbcr.setAllRolesMode("authOnly");
			jdbcr.setConnectionName("");
			jdbcr.setConnectionPassword("");
			jdbcr.setConnectionURL("jdbc:h2:system_data" + File.separator + "system_data;MVCC=TRUE");
			jdbcr.setDriverName("org.h2.Driver");
			jdbcr.setUserCredCol("pwdhash");
			jdbcr.setUserNameCol("userid");
			jdbcr.setUserTable("users");
			jdbcr.setDigest("MD5");
			lr.addRealm(jdbcr);
			context.setRealm(lr);

			SecurityConstraint sc = new SecurityConstraint();
			SecurityCollection scol = new SecurityCollection();
			scol.addPattern("/*");
			sc.addCollection(scol);
			sc.setAuthConstraint(true);
			sc.addAuthRole("*");
			context.addConstraint(sc);

			context.addSecurityRole("*");

			LoginConfig lc = new LoginConfig();
			lc.setAuthMethod("BASIC");
			lc.setRealmName("Anonymous access to " + docBase + " is not allowed");
			context.setLoginConfig(lc);
			context.getPipeline().addValve(new BasicAuthenticator());
		}

		Tomcat.addServlet(context, "SignProvider", "kz.flabs.servlets.eds.SignProvider");
		context.addServletMapping("/SignProvider", "SignProvider");

		Tomcat.addServlet(context, "Login", "kz.flabs.servlets.Login");
		context.addServletMapping("/Login", "Login");

		Tomcat.addServlet(context, "Logout", "kz.flabs.servlets.Logout");
		context.addServletMapping("/Logout", "Logout");

		Wrapper w = Tomcat.addServlet(context, "PortalInit", "kz.flabs.servlets.PortalInit");
		w.setLoadOnStartup(1);

		context.addServletMapping("/PortalInit", "PortalInit");

		Tomcat.addServlet(context, "Uploader", "kz.flabs.servlets.Uploader");
		context.addServletMapping("/Uploader", "Uploader");

		Tomcat.addServlet(context, "UploadFile", "kz.flabs.servlets.UploadFile");
		context.addServletMapping("/UploadFile", "UploadFile");

		Tomcat.addServlet(context, "Error", "kz.flabs.servlets.Error");
		context.addServletMapping("/Error", "Error");

		context.addMimeMapping("css", "text/css");
		context.addMimeMapping("js", "text/javascript");

		return null;
	}

	public Context initDefaultURL() throws LifecycleException, MalformedURLException {
		String db = new File("webapps/Administrator").getAbsolutePath();
		Context context = tomcat.addContext("", db);

		Tomcat.addServlet(context, "Redirector", "kz.flabs.servlets.Redirector");
		context.addServletMapping("/", "Redirector");

		return context;
	}

	public Context initWebServices() throws LifecycleException, MalformedURLException, ServletException {
		String db = new File("webapps/FrontWS").getAbsolutePath();
		Context context = tomcat.addContext("/FrontWS", db);

		context.setDisplayName("WS");
		context.addApplicationListener("org.apache.axis.transport.http.AxisHTTPSessionListener");

		Tomcat.addServlet(context, "default", "org.apache.catalina.servlets.DefaultServlet");
		context.addServletMapping("/", "default");

		Tomcat.addServlet(context, "AxisServlet", "org.apache.axis.transport.http.AxisServlet");
		context.addServletMapping("/AxisServlet", "AxisServlet");
		context.addServletMapping("*.jws", "AxisServlet");
		context.addServletMapping("/services/*", "AxisServlet");

		Wrapper w = Tomcat.addServlet(context, "AdminServlet", "org.apache.axis.transport.http.AdminServlet");
		context.addServletMapping("/AdminServlet", "AdminServlet");
		w.setLoadOnStartup(100);

		w = Tomcat.addServlet(context, "SOAPMonitorService", "org.apache.axis.monitor.SOAPMonitorService");
		context.addServletMapping("/SOAPMonitor", "SOAPMonitorService");
		w.setLoadOnStartup(100);
		w.addInitParameter("SOAPMonitorPort", "5001");

		if (Environment.noWSAuth) {
			return context;
		}

		LockOutRealm lr = new LockOutRealm();

		JDBCRealm jdbcr = new JDBCRealm();
		jdbcr.setAllRolesMode("authOnly");
		jdbcr.setConnectionName("");
		jdbcr.setConnectionPassword("");
		jdbcr.setConnectionURL("jdbc:h2:system_data" + File.separator + "system_data;MVCC=TRUE");
		jdbcr.setDriverName("org.h2.Driver");
		jdbcr.setUserCredCol("pwdhash");
		jdbcr.setUserNameCol("userid");
		jdbcr.setUserTable("users");
		jdbcr.setDigest("MD5");
		lr.addRealm(jdbcr);
		context.setRealm(lr);

		SecurityConstraint sc = new SecurityConstraint();
		SecurityCollection scol = new SecurityCollection();
		scol.addPattern("/*");
		sc.addCollection(scol);
		sc.setAuthConstraint(true);
		sc.addAuthRole("*");
		context.addConstraint(sc);

		context.addSecurityRole("*");

		LoginConfig lc = new LoginConfig();
		lc.setAuthMethod("BASIC");
		lc.setRealmName("Anonymous access to FrontFace services is not allowed");
		context.setLoginConfig(lc);
		context.getPipeline().addValve(new BasicAuthenticator());

		context.addMimeMapping("xsd", "text/html");
		context.addMimeMapping("wsdl", "text/html");
		context.addWelcomeFile("index.jsp");
		context.addWelcomeFile("index.html");
		context.addWelcomeFile("index.jws");

		return context;
	}

	@Override
	public String initConnectors() {
		String portInfo = "";
		if (Environment.isSSLEnable) {
			Connector secureConnector = null;
			kz.pchelka.server.Server.logger.normalLogEntry("TLS connector has been enabled");
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

	@Override
	public void startContainer() {
		try {
			tomcat.start();
			// tomcat.getServer().await();
		} catch (LifecycleException e) {
			kz.pchelka.server.Server.logger.errorLogEntry(e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopContainer();
			}
		});
	}

	@Override
	public synchronized void stopContainer() {
		try {
			if (tomcat != null) {
				tomcat.stop();
			}
		} catch (LifecycleException exception) {
			kz.pchelka.server.Server.logger.errorLogEntry("Cannot Stop WebServer" + exception.getMessage());
		}

	}

}
