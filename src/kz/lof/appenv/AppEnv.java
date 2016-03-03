package kz.lof.appenv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.Application;
import kz.flabs.webrule.GlobalSetting;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.lof.caching.PageCacheAdapter;
import kz.lof.env.Environment;
import kz.lof.server.Server;
import kz.pchelka.env.AuthTypes;
import kz.pchelka.env.Site;
import kz.pchelka.log.ILogger;

public class AppEnv extends PageCacheAdapter implements Const {
	public boolean isValid;
	public String appType = "undefined";
	public WebRuleProvider ruleProvider;
	public HashMap<String, File> xsltFileMap = new HashMap<String, File>();
	public String adminXSLTPath;
	public GlobalSetting globalSetting;
	public boolean isSystem;
	public boolean isWorkspace;
	public AuthTypes authType = AuthTypes.WORKSPACE;
	public Vocabulary vocabulary;
	// public Scheduler scheduler;
	public Application application;
	public static ILogger logger = Server.logger;

	private IDatabase dataBase;

	@Deprecated
	public AppEnv(String at) {
		isSystem = true;
		isValid = true;
		appType = "administrator";
	}

	public AppEnv(String appType, String globalFileName) {
		this.appType = appType;
		try {
			Server.logger.infoLogEntry("# Start application \"" + appType + "\"");
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
					// Server.logger.infoLogEntry("Dictionary is loading...");
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
		this.dataBase = db;
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
		return "[ ]" + Server.serverTitle + "-" + appType;
	}

	public void reloadVocabulary() {
		Server.logger.infoLogEntry("Dictionary is reloading (" + appType + ")...");
		loadVocabulary();
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
			// Server.logger.infoLogEntry("Dictionary has loaded");
		}
	}

}
