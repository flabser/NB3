package kz.lof.appenv;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.lof.administrator.dao.ApplicationDAO;
import kz.lof.administrator.model.Application;
import kz.lof.caching.PageCacheAdapter;
import kz.lof.dataengine.jpa.constants.AppCode;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.log.ILogger;
import kz.lof.rule.RuleProvider;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.SuperUser;

public class AppEnv extends PageCacheAdapter implements Const {
	public boolean isValid;
	public String appName;
	public RuleProvider ruleProvider;
	public HashMap<String, File> xsltFileMap = new HashMap<String, File>();
	public boolean isWorkspace;
	public Vocabulary vocabulary;
	public static ILogger logger = Server.logger;
	private IDatabase dataBase;
	private String rulePath = "rule";

	public AppEnv(String n, IDatabase db) {
		this.appName = n;
		this.dataBase = db;

		if (Environment.isDevMode) {
			ApplicationDAO aDao = new ApplicationDAO(new _Session(this, new SuperUser()));
			Application appliaction = aDao.findByName(appName);
			if (appliaction != null
			        && (appliaction.getCode() == AppCode.ADMINISTRATOR || appliaction.getCode() == AppCode.REFERENCE
			                || appliaction.getCode() == AppCode.WORKSPACE || appliaction.getCode() == AppCode.STAFF)) {
				Path parent = Paths.get(System.getProperty("user.dir")).getParent();
				String extModule = parent + File.separator + EnvConst.OFFICEFRAME;
				rulePath = extModule + File.separator + "rule";
				Server.logger.debugLogEntry("server going to use \"" + appName + "\" as external module (path=" + extModule + ")");
			}
		}

		try {
			ruleProvider = new RuleProvider(this);
			isValid = true;
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}

		loadVocabulary();
	}

	public IDatabase getDataBase() {
		return dataBase;
	}

	@Override
	public String toString() {
		return "[ ]" + Server.serverTitle + "-" + appName;
	}

	public void reloadVocabulary() {
		Server.logger.infoLogEntry("Dictionary is reloading (" + appName + ")...");
		loadVocabulary();
	}

	public String getRulePath() {
		return rulePath;
	}

	private void loadVocabulary() {
		Localizator l = new Localizator();
		String vocabuarFilePath = getRulePath() + File.separator + appName + File.separator + "Resources" + File.separator + "vocabulary.xml";
		vocabulary = l.populate(appName, vocabuarFilePath);
	}

}
