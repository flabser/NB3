package kz.lof.appenv;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
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
	private static final String[] extensions = { "groovy" };

	public AppEnv(String n, IDatabase db) {
		this.appName = n;
		this.dataBase = db;

		if (Environment.isDevMode()) {
			if (EnvConst.ADMINISTRATOR_APP_NAME.equals(appName)) {
				rulePath = Environment.getKernelDir() + "rule";
			} else if (ArrayUtils.contains(EnvConst.OFFICEFRAME_APPS, appName)) {
				rulePath = Environment.getOfficeFrameDir() + "rule";
				Server.logger
				        .debugLogEntry("server going to use \"" + appName + "\" as external module (path=" + Environment.getOfficeFrameDir() + ")");
			}
		}

		try {
			ruleProvider = new RuleProvider(this);
			isValid = true;
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}

		loadVocabulary();
		compileScenarios();
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

	private void compileScenarios() {
		ClassLoader parent = getClass().getClassLoader();
		CompilerConfiguration compiler = new CompilerConfiguration();
		String scriptDirPath = rulePath + File.separator + appName + File.separator + "Resources" + File.separator + "scripts";
		if (Environment.isDevMode()) {
			compiler.setTargetDirectory("bin");
		} else {
			compiler.setTargetDirectory(scriptDirPath);
		}
		GroovyClassLoader loader = new GroovyClassLoader(parent, compiler);

		File cur = new File(scriptDirPath);

		// System.out.println(cur.getAbsolutePath());
		Collection<File> scipts = FileUtils.listFiles(cur, extensions, true);
		for (File groovyFile : scipts) {
			try {
				Server.logger.debugLogEntry("recompile " + groovyFile.getAbsolutePath() + "...");
				Class<GroovyObject> clazz = loader.parseClass(groovyFile);
			} catch (CompilationFailedException e) {
				AppEnv.logger.errorLogEntry(e);
			} catch (IOException e) {
				AppEnv.logger.errorLogEntry(e);
			}
		}
	}

}
