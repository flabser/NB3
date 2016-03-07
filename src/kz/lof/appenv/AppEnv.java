package kz.lof.appenv;

import java.io.File;
import java.util.HashMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.Application;
import kz.lof.caching.PageCacheAdapter;
import kz.lof.rule.RuleProvider;
import kz.lof.server.Server;
import kz.pchelka.log.ILogger;

public class AppEnv extends PageCacheAdapter implements Const {
	public boolean isValid;
	public String appName = "undefined";
	public RuleProvider ruleProvider;
	public HashMap<String, File> xsltFileMap = new HashMap<String, File>();
	public boolean isWorkspace;
	public Vocabulary vocabulary;
	public Application application;
	public static ILogger logger = Server.logger;

	private IDatabase dataBase;

	public AppEnv(String n, String globalFileName) {
		this.appName = n;
		try {
			Server.logger.infoLogEntry("# Start application \"" + appName + "\"");
			ruleProvider = new RuleProvider(this);
			loadVocabulary();
			isValid = true;

		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

	public void setDataBase(IDatabase db) {
		this.dataBase = db;
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

	private void loadVocabulary() {
		Localizator l = new Localizator();
		String vocabuarFilePath = "rule" + File.separator + appName + File.separator + "Resources" + File.separator + "vocabulary.xml";
		vocabulary = l.populate(appName, vocabuarFilePath);
	}

}
