package kz.lof.server;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.catalina.LifecycleException;

import kz.flabs.dataengine.IDatabase;
import kz.lof.env.Environment;
import kz.lof.env.Site;
import kz.lof.log.Log4jLogger;
import kz.lof.scheduler.PeriodicalServices;
import kz.lof.webserver.WebServer;

public class Server {
	public static kz.lof.log.ILogger logger;
	public static final String serverVersion = "3.0.7";
	public static String compilationTime = "";
	public static final String serverTitle = "NextBase " + serverVersion;
	public static Date startTime = new Date();
	public static IDatabase dataBase;
	public static WebServer webServerInst;

	public static void start() throws MalformedURLException, LifecycleException, URISyntaxException {
		logger = new Log4jLogger("Server");
		logger.infoLogEntry(":-)");
		logger.infoLogEntry(serverTitle + " start");
		if (Environment.isDevMode()) {
			Environment.verboseLogging = true;
			logger.warningLogEntry("debug logging is turned on");
		}
		compilationTime = ((Log4jLogger) logger).getBuildDateTime();
		logger.infoLogEntry("Copyright(c) Lab of the Future 2015. All Right Reserved");

		Environment.init();

		webServerInst = new WebServer();
		webServerInst.init(Environment.hostName);

		for (Site webApp : Environment.webAppToStart.values()) {
			webServerInst.addApplication(webApp.siteName, "/" + webApp.name, webApp.name);
		}

		String info = webServerInst.initConnectors();
		Server.logger.debugLogEntry("web server started (" + info + ")");
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
					Environment.setDevMode(true);
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
		logger.infoLogEntry("server is stopping ... ");

		Environment.shutdown();
		if (webServerInst != null) {
			webServerInst.stopContainer();
		}
		logger.infoLogEntry("bye, bye... ");
		System.exit(0);
	}
}
