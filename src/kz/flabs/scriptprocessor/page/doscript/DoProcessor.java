package kz.flabs.scriptprocessor.page.doscript;

import groovy.lang.GroovyObject;
import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.pchelka.server.Server;

public class DoProcessor {

	private LanguageType lang;
	private GroovyObject groovyObject = null;
	private _Session ses;
	private Vocabulary vocabulary;
	private _WebFormData webFormData;

	public DoProcessor(AppEnv env, _Session ses, _WebFormData webFormData) {
		this.ses = ses;
		vocabulary = env.vocabulary;
		lang = ses.getLang();
		this.webFormData = webFormData;
	}

	public PageOutcome processScenario(String className, String method) throws ClassNotFoundException {
		Object object = null;
		try {
			Class<?> pageClass = Class.forName(className);
			object = pageClass.newInstance();
		} catch (InstantiationException e) {
			Server.logger.errorLogEntry(e);
		} catch (IllegalAccessException e) {
			Server.logger.errorLogEntry(e);
		}

		IPageScript myObject = (IPageScript) object;

		myObject.setSession(ses);
		myObject.setFormData(webFormData);
		myObject.setCurrentLang(vocabulary, lang);

		return myObject.processCode(method);
	}

}
