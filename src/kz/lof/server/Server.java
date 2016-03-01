package kz.lof.server;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;

import kz.flabs.dataengine.IDatabase;
import kz.lof.env.Environment;
import kz.lof.scheduler.PeriodicalServices;
import kz.lof.webserver.WebServer;
import kz.pchelka.env.Site;
import kz.pchelka.log.Log4jLogger;

import org.apache.catalina.LifecycleException;

public class Server {
	public static kz.pchelka.log.ILogger logger;
	public static final String serverVersion = "3.0.5";
	public static String compilationTime = "";
	public static final String serverTitle = "NextBase " + serverVersion;
	public static Date startTime = new Date();
	public static IDatabase dataBase;
	public static WebServer webServerInst;

	public static void start() throws MalformedURLException, LifecycleException, URISyntaxException {
		logger = new Log4jLogger("Server");
		logger.infoLogEntry(serverTitle + " start");
		if (Environment.isDevMode) {
			Environment.verboseLogging = true;
			logger.warningLogEntry("Debug logging is turned on");
		}
		compilationTime = ((Log4jLogger) logger).getBuildDateTime();
		logger.infoLogEntry("Copyright(c) Lab of the Future 2014. All Right Reserved");

		Environment.init();

		webServerInst = new WebServer();
		webServerInst.init(Environment.hostName);

		for (Site webApp : Environment.webAppToStart.values()) {
			webServerInst.addApplication(webApp.name, "/" + webApp.appBase, webApp.appBase);
		}

		String info = webServerInst.initConnectors();
		Server.logger.debugLogEntry("Web server started (" + info + ")");
		webServerInst.startContainer();

		Environment.periodicalServices = new PeriodicalServices();

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
		logger.infoLogEntry("Server is stopping ... ");

		Environment.shutdown();
		webServerInst.stopContainer();
		System.exit(0);
	}
}
