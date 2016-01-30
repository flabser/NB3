package kz.pchelka.server;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Timer;

import kz.flabs.dataengine.IDatabase;
import kz.pchelka.console.rmi.server.RMIServer;
import kz.pchelka.env.Environment;
import kz.pchelka.env.Site;
import kz.pchelka.log.Log4jLogger;
import kz.pchelka.webserver.IWebServer;
import kz.pchelka.webserver.WebServerFactory;

import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;

public class Server {
	public static kz.pchelka.log.ILogger logger;
	public static final String serverVersion = "2.8.5";
	public static final int necessaryDbVersion = 104;
	public static String compilationTime = "";
	public static final String serverTitle = "NextBase " + serverVersion + "-" + Integer.toString(necessaryDbVersion);
	public static Date startTime = new Date();
	public static IDatabase dataBase;
	public static IWebServer webServerInst;
	public static RMIServer rmiServer;

	public static void start() throws MalformedURLException, LifecycleException, URISyntaxException {
		logger = new Log4jLogger("Server");
		logger.normalLogEntry(serverTitle + " start");
		compilationTime = ((Log4jLogger) logger).getBuildDateTime();
		logger.verboseLogEntry("Build " + compilationTime);
		logger.normalLogEntry("Copyright(c) Lab of the Future 2014. All Right Reserved");
		logger.normalLogEntry("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "("
				+ System.getProperty("os.arch") + "), jvm: " + System.getProperty("java.version"));

		Environment.init();

		webServerInst = WebServerFactory.getServer(Environment.serverVersion);
		webServerInst.init(Environment.hostName);

		if (Environment.adminConsoleEnable) {
			Host host = webServerInst.addApplication("Administrator", "/Administrator", "Administrator");
			if (Environment.remoteConsole) {
				rmiServer = RMIServer.getInstance();
			}
			HashSet<Host> hosts = new HashSet<Host>();
			hosts.add(host);
		}

		kz.pchelka.server.Server.logger.normalLogEntry("All applications are starting...");

		for (Site webApp : Environment.webAppToStart.values()) {
			// hosts.add(webServerInst.addApplication(webApp.name, "/" +
			// webApp.appBase, webApp.appBase));
			webServerInst.addApplication(webApp.name, "/" + webApp.appBase, webApp.appBase);
		}

		String info = webServerInst.initConnectors();
		kz.pchelka.server.Server.logger.verboseLogEntry("Web server started (" + info + ")");
		webServerInst.startContainer();
		MemoryInspector inspector = new MemoryInspector(logger);
		Timer mtimer = new java.util.Timer();
		mtimer.schedule(inspector, 1000 * 60, 1000 * 60 * 5);

		Thread thread = new Thread(new Console());
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	public static void main(String[] arg) {
		try {
			for (int i = 0; i < arg.length; i++) {
				if (arg[i].equals("developing")) {
					Environment.isDevMode = true;
				}
			}
			Server.start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (LifecycleException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void shutdown() {
		logger.normalLogEntry("Server is stopping ... ");
		for (Entry<String, IDatabase> db : Environment.getDatabases().entrySet()) {
			db.getValue().shutdown();
		}
		Environment.shutdown();
		webServerInst.stopContainer();
		System.exit(0);
	}
}
