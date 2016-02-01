package kz.flabs.appenv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.Application;
import kz.flabs.runtimeobj.caching.ICache;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.webrule.GlobalSetting;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.pchelka.env.AuthTypes;
import kz.pchelka.env.Environment;
import kz.pchelka.env.Site;
import kz.pchelka.log.ILogger;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.scheduler.Scheduler;
import kz.pchelka.server.Server;

public class AppEnv implements Const, ICache, IProcessInitiator {
	public boolean isValid;
	public String appType = "undefined";
	public WebRuleProvider ruleProvider;
	public HashMap<String, File> xsltFileMap = new HashMap<String, File>();
	// public String rulePath;
	public String adminXSLTPath;
	public GlobalSetting globalSetting;
	public boolean isSystem;
	public boolean isWorkspace;
	public AuthTypes authType = AuthTypes.WORKSPACE;
	public Vocabulary vocabulary;
	public Scheduler scheduler;
	public Application application;
	public static ILogger logger = Server.logger;

	private IDatabase dataBase;
	private HashMap<String, StringBuffer> cache = new HashMap<String, StringBuffer>();

	@Deprecated
	public AppEnv(String at) {
		isSystem = true;
		isValid = true;
		appType = "administrator";
	}

	public AppEnv(String appType, String globalFileName) {
		this.appType = appType;
		try {
			Server.logger.normalLogEntry("# Start application \"" + appType + "\"");
			Site appSite = Environment.webAppToStart.get(appType);
			if (appSite != null) {
				authType = appSite.authType;
			}
			// rulePath = "rule" + File.separator + appType;
			ruleProvider = new WebRuleProvider(this);
			ruleProvider.initApp(globalFileName);
			globalSetting = ruleProvider.global;
			globalSetting.appName = appType;
			isWorkspace = globalSetting.isWorkspace;
			if (globalSetting.isOn == RunMode.ON) {
				if (globalSetting.langsList.size() > 0) {
					Server.logger.normalLogEntry("Dictionary is loading...");
					loadVocabulary();
				}
				isValid = true;
			} else {
				Server.logger.warningLogEntry("Application: \"" + appType + "\" is off");
				Environment.reduceApplication();
			}

		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
			// e.printStackTrace();
		}
	}

	public void setDataBase(IDatabase db) {
		if (!db.getDbID().equalsIgnoreCase("NoDatabase")) {
			int cv = db.getVersion();

			this.dataBase = db;
			// checkLangsSupport();
		} else {
			this.dataBase = db;
		}
	}

	public ArrayList<Role> getRolesList() {
		// ArrayList<Role> rolesList = (ArrayList<Role>)
		// globalSetting.roleCollection.getRolesList().clone();
		for (AppEnv extApp : Environment.getApplications()) {
			for (ExternalModule module : extApp.globalSetting.extModuleMap.values()) {
				if (module.getType() == ExternalModuleType.STRUCTURE && module.getName().equalsIgnoreCase(appType)) {
					// rolesList.addAll(extApp.getRolesList());
				}
			}
		}
		// return rolesList;
		return null;
	}

	public HashMap<String, Role> getRolesMap() {
		// HashMap<String, Role> rolesMap = (HashMap<String, Role>)
		// globalSetting.roleCollection.getRolesMap().clone();
		for (AppEnv extApp : Environment.getApplications()) {
			for (ExternalModule module : extApp.globalSetting.extModuleMap.values()) {
				if (module.getType() == ExternalModuleType.STRUCTURE && module.getName().equalsIgnoreCase(appType)) {
					// rolesMap.putAll(extApp.getRolesMap());
				}
			}
		}
		// return rolesMap;
		return null;
	}

	public IDatabase getDataBase() {
		return dataBase;
	}

	@Override
	public String toString() {
		return Server.serverTitle + "-" + appType;
	}

	public void reloadVocabulary() {
		Server.logger.normalLogEntry("Dictionary is reloading (" + appType + ")...");
		loadVocabulary();
	}

	@Override
	public StringBuffer getPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, QueryFormulaParserException,
	        DocumentException, DocumentAccessException, QueryException {
		boolean reload = false;
		Object obj = cache.get(page.getID());
		String p[] = formData.get("cache");
		if (p != null) {
			String cacheParam = formData.get("cache")[0];
			if (cacheParam.equalsIgnoreCase("reload")) {
				reload = true;
			}
		}
		if (obj == null || reload) {
			StringBuffer buffer = page.getContent(formData, "GET");
			cache.put(page.getID(), buffer);
			return buffer;
		} else {
			return (StringBuffer) obj;
		}

	}

	@Override
	public void flush() {
		cache.clear();
	}

	@Override
	public String getOwnerID() {
		return appType;
	}

	@Deprecated
	public static String getName() {
		return "appType";
	}

	private void loadVocabulary() {
		Localizator l = new Localizator();
		String vocabuarFilePath = globalSetting.rulePath + File.separator + "Resources" + File.separator + "vocabulary.xml";
		vocabulary = l.populate(appType, vocabuarFilePath);
		if (vocabulary != null) {
			Server.logger.normalLogEntry("Dictionary has loaded");
		}
	}

	public String getCacheInfo() {
		String ci = "";
		for (String c : cache.keySet()) {
			ci = ci + "," + c;
		}
		if (ci.equals("")) {
			ci = "cache is empty";
		}
		return ci;
	}

}
