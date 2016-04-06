package kz.lof.appenv;

import java.io.File;
import java.util.HashMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.lof.caching.PageCacheAdapter;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.log.ILogger;
import kz.lof.rule.RuleProvider;
import kz.lof.server.Server;

import org.apache.commons.lang3.ArrayUtils;

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

		if (Environment.isDevMode()) {
			if (EnvConst.ADMINISTRATOR_APP_NAME.equals(appName)) {
				rulePath = Environment.getKernelDir() + "rule";
				Server.logger.debugLogEntry("server going to use \"" + appName + "\" as external module");

			} else if (ArrayUtils.contains(EnvConst.OFFICEFRAME_APPS, appName)) {
				rulePath = Environment.getOfficeFrameDir() + "rule";
				Server.logger.debugLogEntry("server going to use \"" + appName + "\" as external module (path=" + Environment.getOfficeFrameDir()
				        + ")");
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
