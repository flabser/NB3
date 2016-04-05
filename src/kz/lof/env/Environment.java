package kz.lof.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IDatabaseDeployer;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.caching.ICache;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.util.XMLUtil;
import kz.lof.appenv.AppEnv;
import kz.lof.localization.LanguageCode;
import kz.lof.scheduler.PeriodicalServices;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scriptprocessor.page.PageOutcome;
import kz.lof.server.Server;
import net.sf.saxon.s9api.SaxonApiException;

import org.jdom.input.SAXHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Environment implements Const, ICache {
	public static boolean verboseLogging;
	public static Date startTime;
	public static String orgName;
	public static String hostName;
	public static int httpPort = EnvConst.DEFAULT_HTTP_PORT;
	public static String httpSchema = "http";
	public static IDatabase dataBase;
	public static String defaultSender = "";
	public static HashMap<String, String> mimeHash = new HashMap<String, String>();
	public static HashMap<String, Site> webAppToStart = new HashMap<String, Site>();
	public static String tmpDir;
	public static String trash;
	public static ArrayList<String> fileToDelete = new ArrayList<String>();

	public static Boolean isSSLEnable = false;
	public static int secureHttpPort;
	public static String keyPwd = "";
	public static String keyStore = "";
	public static String trustStore;
	public static String trustStorePwd;
	public static boolean isClientSSLAuthEnable;

	public static List<LanguageCode> langs = new ArrayList<LanguageCode>();

	public static String smtpPort;
	public static boolean smtpAuth;
	public static String SMTPHost;
	public static String smtpUser;
	public static String smtpPassword;
	public static Boolean mailEnable = false;
	public static Vocabulary vocabulary;
	public static String workspaceName = "Workspace";
	public static String primaryAppDir = "";
	public static PeriodicalServices periodicalServices;
	public static final String vocabuarFilePath = "resources" + File.separator + "vocabulary.xml";

	private static String defaultRedirectURL;

	private static HashMap<String, AppEnv> applications = new HashMap<String, AppEnv>();
	private static ConcurrentHashMap<String, AppEnv> allApplications = new ConcurrentHashMap<String, AppEnv>();

	private static HashMap<String, Object> cache = new HashMap<String, Object>();
	private static ArrayList<IDatabase> delayedStart = new ArrayList<IDatabase>();
	private static ArrayList<_Session> sess = new ArrayList<_Session>();
	private static boolean isDevMode;
	private static String officeFrameDir = "";
	private static String kernelDir = "";

	public static void init() {
		startTime = new Date();
		loadProperties();
		initProcess();
		try {
			IDatabase db = new kz.lof.dataengine.jpadatabase.Database();
			IDatabaseDeployer dd = new kz.lof.dataengine.jpadatabase.DatabaseDeployer(db);
			dd.deploy();
			Environment.dataBase = db;
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

			Server.logger.infoLogEntry("initialize runtime environment");
			initMimeTypes();

			orgName = XMLUtil.getTextContent(xmlDocument, "/nextbase/orgname");
			if (orgName.isEmpty()) {
				hostName = EnvConst.APP_NAME;
			}

			hostName = XMLUtil.getTextContent(xmlDocument, "/nextbase/hostname");
			if (hostName.isEmpty()) {
				hostName = getHostName();
			}

			String portAsText = XMLUtil.getTextContent(xmlDocument, "/nextbase/port");
			try {
				httpPort = Integer.parseInt(portAsText);
				Server.logger.infoLogEntry("WebServer is going to use port: " + httpPort);
			} catch (NumberFormatException nfe) {
				Server.logger.infoLogEntry("WebServer is going to use default port (" + httpPort + ")");
			}

			NodeList nodeList = XMLUtil.getNodeList(xmlDocument, "/nextbase/applications");
			if (nodeList.getLength() > 0) {
				org.w3c.dom.Element root = xmlDocument.getDocumentElement();
				NodeList nodes = root.getElementsByTagName("app");
				for (int i = 0; i < nodes.getLength(); i++) {
					Node appNode = nodes.item(i);
					if (XMLUtil.getTextContent(appNode, "name/@mode", false).equals("on")) {
						String appName = XMLUtil.getTextContent(appNode, "name", false);
						Site site = new Site();
						site.name = appName;
						site.siteName = XMLUtil.getTextContent(appNode, "name/@sitename", false);
						webAppToStart.put(appName, site);
					}
				}
			}

			// TODO Need to add exception handler
			NodeList l = XMLUtil.getNodeList(xmlDocument, "/nextbase/langs/entry");
			for (int i = 0; i < l.getLength(); i++) {
				langs.add(LanguageCode.valueOf(XMLUtil.getTextContent(l.item(i), ".", false)));
			}

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
					Server.logger.infoLogEntry("TLS is enabled");
					httpSchema = "https";
				}
			} catch (Exception ex) {
				Server.logger.infoLogEntry("TLS configiration error");
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
					Server.logger.infoLogEntry("mailAgent is going to redirect some messages to host: " + SMTPHost);
				} else {
					Server.logger.infoLogEntry("mailAgent is switch off");
				}
			} catch (NumberFormatException nfe) {
				Server.logger.infoLogEntry("MailAgent is not set");
				SMTPHost = "";
				defaultSender = "";
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

		} catch (SAXException se) {
			Server.logger.errorLogEntry(se);
		} catch (ParserConfigurationException pce) {
			Server.logger.errorLogEntry(pce);
		} catch (IOException ioe) {
			Server.logger.errorLogEntry(ioe);
		}
	}

	public static void addApplication(AppEnv env) {
		applications.put(env.appName, env);
		allApplications.put(env.appName, env);
		allApplications.put(env.appName.toLowerCase(), env);
	}

	public static void addDelayedInit(IDatabase db) {
		delayedStart.add(db);
	}

	public static AppEnv getAppEnv(String appID) {
		return allApplications.get(appID);
	}

	public static AppEnv getApplication(String appID) {
		return applications.get(appID);
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
			Server.logger.errorLogEntry(e);
		} catch (IOException e) {
			Server.logger.errorLogEntry(e);
		} catch (ParserConfigurationException e) {
			Server.logger.errorLogEntry(e);
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
			cachesList.add(env.appName + ":" + ci);
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
	public PageOutcome getCachedPage(PageOutcome outcome, Page page, _WebFormData formData) throws ClassNotFoundException, RuleException,
	        IOException, SaxonApiException {
		String cacheKey = page.getCacheID();
		Object obj = cache.get(cacheKey);
		String cacheParam[] = formData.getFormData().get("cache");
		if (cacheParam == null) {
			PageOutcome buffer = page.getPageContent(outcome, formData, "GET");
			cache.put(cacheKey, buffer.getValue());
			return buffer;
		} else if (cacheParam[0].equalsIgnoreCase("reload")) {
			PageOutcome buffer = page.getPageContent(outcome, formData, "GET");
			cache.put(cacheKey, buffer.getValue());
			return buffer;
		} else {
			return (PageOutcome) obj;
		}

	}

	public static boolean isDevMode() {
		return isDevMode;
	}

	public static void setDevMode(boolean isDevMode) {
		Environment.isDevMode = isDevMode;
		Path parent = Paths.get(System.getProperty("user.dir")).getParent();
		officeFrameDir = parent + File.separator + EnvConst.OFFICEFRAME + File.separator;
		kernelDir = parent + File.separator + EnvConst.FRAMEWORK_NAME + File.separator;
	}

	public static String getOfficeFrameDir() {
		return officeFrameDir;
	}

	public static String getKernelDir() {
		return kernelDir;
	}
}
