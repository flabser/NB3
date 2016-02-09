package kz.pchelka.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.caching.ICache;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.env.EnvConst;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.pchelka.daemon.system.LogsZipRule;
import kz.pchelka.daemon.system.TempFileCleanerRule;
import kz.pchelka.log.ILogger;
import kz.pchelka.scheduler.IDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.scheduler.Scheduler;
import kz.pchelka.server.Server;
import net.sf.saxon.s9api.SaxonApiException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Environment implements Const, ICache, IProcessInitiator {

	public static boolean verboseLogging;
	public static int serverVersion;
	public static Boolean webServicesEnable = false;
	public static String serverName;
	public static String hostName;
	public static int httpPort = 38779;
	public static boolean noWSAuth = false;
	public static String httpSchema = "http";

	public static ISystemDatabase systemBase;
	public static String defaultSender = "";
	public static HashMap<String, String> mimeHash = new HashMap<String, String>();
	public static boolean adminConsoleEnable;
	public static HashMap<String, Site> webAppToStart = new HashMap<String, Site>();
	public static String tmpDir;
	public static String trash;
	public static ArrayList<String> fileToDelete = new ArrayList<String>();
	public static ILogger logger;
	public static int delaySchedulerStart;
	public static Scheduler scheduler = new Scheduler();

	public static Boolean isSSLEnable = false;
	public static int secureHttpPort;
	public static String keyPwd = "";
	public static String keyStore = "";
	public static String trustStore;
	public static String trustStorePwd;
	public static boolean isClientSSLAuthEnable;

	public static List<LanguageType> langs = new ArrayList<LanguageType>();

	public static String smtpPort;
	public static boolean smtpAuth;
	public static String SMTPHost;
	public static String smtpUser;
	public static String smtpPassword;
	public static Boolean mailEnable = false;

	public static boolean workspaceAuth;
	private static String defaultRedirectURL;

	public static RunMode debugMode = RunMode.OFF;

	private static boolean schedulerStarted;
	private static HashMap<String, ExternalHost> externalHost = new HashMap<String, ExternalHost>();
	private static HashMap<String, AppEnv> applications = new HashMap<String, AppEnv>();
	private static HashMap<String, AppEnv> allApplications = new HashMap<String, AppEnv>();
	private static HashMap<String, IDatabase> dataBases = new HashMap<String, IDatabase>();

	private static HashMap<String, Object> cache = new HashMap<String, Object>();
	private static int countOfApp;
	private static ArrayList<IDatabase> delayedStart = new ArrayList<IDatabase>();
	private static Integer rtfLimitSize = 3892;

	private static ArrayList<_Session> sess = new ArrayList<_Session>();
	public static boolean isDevMode;
	public static Vocabulary vocabulary;
	public static String workspaceName = "Workspace";
	public static String primaryAppDir = "";
	public static final String vocabuarFilePath = "resources" + File.separator + "vocabulary.xml";

	public static void init() {
		logger = Server.logger;
		loadProperties();
		initProcess();
		try {
			Environment.systemBase = new kz.flabs.dataengine.h2.SystemDatabase();
		} catch (DatabasePoolException e) {
			Server.logger.errorLogEntry(e);
			Server.logger.fatalLogEntry("Server has not connected to system database");
			Server.shutdown();
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
			Server.shutdown();
		}
	}

	private static void initProcess() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			SAXParser saxParser = factory.newSAXParser();
			SAXHandler cfgXMLhandler = new SAXHandler();
			File file = new File("cfg.xml");
			saxParser.parse(file, cfgXMLhandler);
			Document xmlDocument = getDocument();

			logger.normalLogEntry("Initialize runtime environment");
			initMimeTypes();

			serverVersion = XMLUtil.getNumberContent(xmlDocument, "/nextbase/webserver/version", 7);

			webServicesEnable = XMLUtil.getTextContent(xmlDocument, "/nextbase/webserver/webservices/@mode").equalsIgnoreCase("on");

			hostName = XMLUtil.getTextContent(xmlDocument, "/nextbase/hostname");
			if (hostName.isEmpty()) {
				hostName = getHostName();
			}

			serverName = XMLUtil.getTextContent(xmlDocument, "/nextbase/name");
			String portAsText = XMLUtil.getTextContent(xmlDocument, "/nextbase/port");
			try {
				httpPort = Integer.parseInt(portAsText);
				logger.normalLogEntry("WebServer is going to use port: " + httpPort);
			} catch (NumberFormatException nfe) {
				logger.normalLogEntry("WebServer is going to use standart port");
			}

			try {
				String auth = XMLUtil.getTextContent(xmlDocument, "/nextbase/no-ws-auth");
				if ("true".equalsIgnoreCase(auth)) {
					noWSAuth = true;
				}
			} catch (Exception e) {
				noWSAuth = false;
			}

			delaySchedulerStart = XMLUtil.getNumberContent(xmlDocument, "/nextbase/scheduler/startdelaymin", 1);

			defaultRedirectURL = "/" + XMLUtil.getTextContent(xmlDocument, "/nextbase/applications/@default", false, "Workspace", true);

			NodeList nodeList = XMLUtil.getNodeList(xmlDocument, "/nextbase/applications");
			if (nodeList.getLength() > 0) {
				org.w3c.dom.Element root = xmlDocument.getDocumentElement();
				NodeList nodes = root.getElementsByTagName("app");
				for (int i = 0; i < nodes.getLength(); i++) {
					Node appNode = nodes.item(i);
					if (XMLUtil.getTextContent(appNode, "name/@mode", false).equals("on")) {
						String appName = XMLUtil.getTextContent(appNode, "name", false);
						Site site = new Site();
						site.appBase = appName;
						site.authType = AuthTypes.valueOf(XMLUtil.getTextContent(appNode, "authtype", false, "WORKSPACE", false));
						site.name = XMLUtil.getTextContent(appNode, "name/@sitename", false);
						String globalAttrValue = XMLUtil.getTextContent(appNode, "name/@global", false);
						if (!globalAttrValue.isEmpty()) {
							site.global = globalAttrValue;
						}
						webAppToStart.put(appName, site);
					}
				}
			}

			// TODO Need to add exception handler
			NodeList l = XMLUtil.getNodeList(xmlDocument, "/nextbase/langs/entry");
			for (int i = 0; i < l.getLength(); i++) {
				langs.add(LanguageType.valueOf(XMLUtil.getTextContent(l.item(i), ".", false)));
			}

			countOfApp = webAppToStart.size();

			try {
				isSSLEnable = XMLUtil.getTextContent(xmlDocument, "/nextbase/ssl/@mode").equalsIgnoreCase("on");
				if (isSSLEnable) {
					secureHttpPort = XMLUtil.getNumberContent(xmlDocument, "/nextbase/ssl/port", 38789);
					keyPwd = XMLUtil.getTextContent(xmlDocument, "/nextbase/ssl/keypass");
					keyStore = XMLUtil.getTextContent(xmlDocument, "/nextbase/ssl/keystore");
					isClientSSLAuthEnable = XMLUtil.getTextContent(xmlDocument, "/nextbase/ssl/clientauth/@mode").equalsIgnoreCase("on");
					if (isClientSSLAuthEnable) {
						trustStore = XMLUtil.getTextContent(xmlDocument, "/nextbase/ssl/clientauth/truststorefile");
						trustStorePwd = XMLUtil.getTextContent(xmlDocument, "/nextbase/ssl/clientauth/truststorepass");
					}
					// logger.normalLogEntry("SSL is enabled. keyPass: " +
					// keyPwd +", keyStore:" +
					// keyStore);
					logger.normalLogEntry("TLS is enabled");
					httpSchema = "https";
				}
			} catch (Exception ex) {
				logger.normalLogEntry("TLS configiration error");
				isSSLEnable = false;
				keyPwd = "";
				keyStore = "";
			}

			try {
				mailEnable = XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/@mode").equalsIgnoreCase("on") ? true : false;
				if (mailEnable) {
					SMTPHost = XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/smtphost");
					defaultSender = XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/defaultsender");
					smtpAuth = Boolean.valueOf(XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/auth"));
					smtpUser = XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/smtpuser");
					smtpPassword = XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/smtppassword");
					smtpPort = XMLUtil.getTextContent(xmlDocument, "/nextbase/mailagent/smtpport");
					logger.normalLogEntry("MailAgent is going to redirect some messages to host: " + SMTPHost);
				} else {
					logger.normalLogEntry("MailAgent is switch off");
				}
			} catch (NumberFormatException nfe) {
				logger.normalLogEntry("MailAgent is not set");
				SMTPHost = "";
				defaultSender = "";
			}

			try {
				String res = XMLUtil.getTextContent(xmlDocument, "/nextbase/logging/verbose");
				if (res.equalsIgnoreCase("true")) {
					verboseLogging = true;
					logger.warningLogEntry("Verbose logging is turned on");
				}
			} catch (Exception e) {
				verboseLogging = false;
			}

			try {
				Integer limitSize = XMLUtil.getNumberContent(xmlDocument, "/nextbase/rtfcontent/limitsize");
				if (limitSize > 0) {
					rtfLimitSize = limitSize;
				}
			} catch (NumberFormatException nfe) {
				logger.normalLogEntry("Default size for rtfcontent field is " + rtfLimitSize + " mb");
			}

			File tmp = new File("tmp");
			if (!tmp.exists()) {
				tmp.mkdir();
			}

			tmpDir = tmp.getAbsolutePath();

			File jrDir = new File(tmpDir + File.separator + "trash");
			if (!jrDir.exists()) {
				jrDir.mkdir();
			}

			trash = jrDir.getAbsolutePath();

			Localizator lz = new Localizator();
			vocabulary = lz.populate();
			if (vocabulary == null) {
				vocabulary = new Vocabulary("system");
			}

			NodeList hosts = XMLUtil.getNodeList(xmlDocument, "/nextbase/externalhost/host");
			for (int i = 0; i < hosts.getLength(); i++) {
				ExternalHost host = new ExternalHost(hosts.item(i));
				if (host.isOn != RunMode.OFF && host.isValid) {
					externalHost.put(host.id.toLowerCase(), host);
				}
			}

			if (XMLUtil.getTextContent(xmlDocument, "/nextbase/debug/@mode").equalsIgnoreCase("on")) {
				debugMode = RunMode.ON;
			}

			{
				TempFileCleanerRule tfcr = new TempFileCleanerRule();
				tfcr.init(new Environment());
				try {
					Class c = Class.forName(tfcr.getClassName());
					IDaemon daemon = (IDaemon) c.newInstance();
					daemon.init(tfcr);
					scheduler.addProcess(tfcr, daemon);
				} catch (InstantiationException e) {
					logger.errorLogEntry(e);
				} catch (IllegalAccessException e) {
					logger.errorLogEntry(e);
				} catch (ClassNotFoundException e) {
					logger.errorLogEntry(e);
				}
			}

			{
				LogsZipRule lzr = new LogsZipRule();
				lzr.init(new Environment());
				try {
					Class c = Class.forName(lzr.getClassName());
					IDaemon daemon = (IDaemon) c.newInstance();
					daemon.init(lzr);
					scheduler.addProcess(lzr, daemon);
				} catch (InstantiationException e) {
					logger.errorLogEntry(e);
				} catch (IllegalAccessException e) {
					logger.errorLogEntry(e);
				} catch (ClassNotFoundException e) {
					logger.errorLogEntry(e);
				}
			}

			{
				// BackupServiceRule bsr = new BackupServiceRule();
				// bsr.init(new Environment());
				// try{
				// Class c = Class.forName(bsr.getClassName());
				// IDaemon daemon = (IDaemon)c.newInstance();
				// daemon.init(bsr);
				// scheduler.addProcess(bsr, daemon);
				// }catch (InstantiationException e) {
				// logger.errorLogEntry(e);
				// } catch (IllegalAccessException e) {
				// logger.errorLogEntry(e);
				// } catch (ClassNotFoundException e) {
				// logger.errorLogEntry(e);
				// }
			}
		} catch (SAXException se) {
			logger.errorLogEntry(se);
		} catch (ParserConfigurationException pce) {
			logger.errorLogEntry(pce);
		} catch (IOException ioe) {
			logger.errorLogEntry(ioe);
		}
	}

	public static void reduceApplication() {
		countOfApp--;
	}

	public static Integer getRtfLimitSize() {
		return rtfLimitSize;
	}

	public static void addApplication(AppEnv env) {
		applications.put(env.appType, env);
		allApplications.put(env.appType, env);
		allApplications.put(env.appType.toLowerCase(), env);
		if (env.isWorkspace) {
			workspaceAuth = true;
		}

		if (applications.size() >= countOfApp) {
			if (delayedStart.size() > 0) {
				for (IDatabase db : delayedStart) {
					// logger.normalLogEntry("Connecting to external module " +
					// db.initExternalPool(ExternalModuleType.STRUCTURE));
					if (!schedulerStarted) {
						Thread schedulerThread = new Thread(scheduler);
						schedulerThread.start();
						schedulerStarted = true;
					}
				}
			} else {
				Thread schedulerThread = new Thread(scheduler);
				schedulerThread.start();
				schedulerStarted = true;
			}
			delayedStart = new ArrayList<>();
		}
	}

	public static void addDelayedInit(IDatabase db) {
		delayedStart.add(db);
	}

	public static void addDatabases(IDatabase dataBase) {
		dataBases.put(dataBase.getDbID(), dataBase);
	}

	public static AppEnv getAppEnv(String appID) {
		return allApplications.get(appID);
	}

	public static AppEnv getApplication(String appID) {
		return applications.get(appID);
	}

	public static IDatabase getDatabase(String dbID) {
		return dataBases.get(dbID);
	}

	public static HashMap<String, IDatabase> getDatabases() {
		return dataBases;
	}

	public static Collection<AppEnv> getApplications() {
		return new HashSet<AppEnv>(applications.values());
	}

	public static String getFullHostName() {
		return httpSchema + "://" + Environment.hostName + ":" + Environment.httpPort;
	}

	public static String getDefaultRedirectURL() {
		return defaultRedirectURL;
	}

	public static String getWorkspaceURL() {
		return "Workspace";
	}

	public static String getExtHost(String id) {
		String h = externalHost.get(id.toLowerCase()).host;
		return h;
	}

	public static ExternalHost getExternalHost(String id) {
		return externalHost.get(id.toLowerCase());
	}

	public static void addExtHost(String id, String host, String name) {
		externalHost.put(id.toLowerCase(), new ExternalHost(id.toLowerCase(), host, name));
		return;
	}

	private static void initMimeTypes() {
		mimeHash.put("pdf", "application/pdf");
		mimeHash.put("doc", "application/msword");
		mimeHash.put("xls", "application/vnd.ms-excel");
		mimeHash.put("tif", "image/tiff");
		mimeHash.put("rtf", "application/msword");
		mimeHash.put("gif", "image/gif");
		mimeHash.put("jpg", "image/jpeg");
		mimeHash.put("html", "text/html");
		mimeHash.put("zip", "application/zip");
		mimeHash.put("rar", "application/x-rar-compressed");
	}

	private static Document getDocument() {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;

			builder = domFactory.newDocumentBuilder();
			return builder.parse("cfg.xml");
		} catch (SAXException e) {
			logger.errorLogEntry(e);
		} catch (IOException e) {
			logger.errorLogEntry(e);
		} catch (ParserConfigurationException e) {
			logger.errorLogEntry(e);
		}
		return null;
	}

	private static String getHostName() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return addr.getHostName();
	}

	@Override
	public void flush() {
		cache.clear();
	}

	public static void flushSessionsCach() {
		for (_Session ses : sess) {
			ses.flush();
		}
	}

	public static List<String> getSessionCachesInfo() {
		List<String> cachesList = new ArrayList<String>();
		for (_Session ses : sess) {
			String ci = ses.getCacheInfo();
			if (ci.equals("")) {
				ci = "cache is empty";
			}
			cachesList.add(ses.getUser().getUserID() + ":" + ci);
		}
		return cachesList;
	}

	public static List<String> getAppsCachesInfo() {
		List<String> cachesList = new ArrayList<String>();
		for (AppEnv env : applications.values()) {
			String ci = env.getCacheInfo();
			cachesList.add(env.appType + ":" + ci);
		}
		return cachesList;
	}

	public static String getCacheInfo() {
		String ci = "";
		for (String c : cache.keySet()) {
			// ci = ci + "," + c + "\n" + cache.get(c);
			ci = ci + "," + c;
		}
		if (ci.equals("")) {
			ci = "cache is empty";
		}
		return ci;
	}

	public static void shutdown() {
		// if (XMPPServerEnable) Environment.connection.disconnect();
	}

	@Override
	public String getOwnerID() {
		return "";
	}

	private static void loadProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("resources" + File.separator + "config.properties");

			prop.load(input);
			Field[] declaredFields = EnvConst.class.getDeclaredFields();
			for (Field field : declaredFields) {
				if (Modifier.isStatic(field.getModifiers())) {
					String value = prop.getProperty(field.getName());
					if (value != null) {
						field.set(String.class, prop.getProperty(field.getName()));
					}
				}
			}
		} catch (FileNotFoundException e) {

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	@Override
	public PageOutcome getCachedPage(Page page, _WebFormData formData) throws ClassNotFoundException, RuleException, IOException, SaxonApiException {
		String cacheKey = page.getCacheID();
		Object obj = cache.get(cacheKey);
		String cacheParam[] = formData.getFormData().get("cache");
		if (cacheParam == null) {
			PageOutcome buffer = page.getPageContent(formData, "GET");
			cache.put(cacheKey, buffer.getValue());
			return buffer;
		} else if (cacheParam[0].equalsIgnoreCase("reload")) {
			PageOutcome buffer = page.getPageContent(formData, "GET");
			cache.put(cacheKey, buffer.getValue());
			return buffer;
		} else {
			return (PageOutcome) obj;
		}

	}
}
