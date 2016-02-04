package kz.flabs.scriptprocessor.page.doscript;

import groovy.lang.GroovyObject;

import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.flabs.users.User;
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

	@Deprecated
	public DoProcessor(AppEnv env, User u, LanguageType currentLang, Map<String, String[]> formData) {
		ses = new _Session(env, u);
		vocabulary = env.vocabulary;
		lang = currentLang;
		webFormData = new _WebFormData(formData);
	}

	@Deprecated
	public DoProcessor(AppEnv env, User user, String lang, Map<String, String[]> formData) {
		ses = new _Session(env, user);
		vocabulary = env.vocabulary;
		lang = lang;
		webFormData = new _WebFormData(formData);
	}

	public DoProcessor(AppEnv env, _Session ses, Map<String, String[]> formData) {
		this.ses = ses;
		vocabulary = env.vocabulary;
		lang = ses.getLang();
		webFormData = new _WebFormData(formData);
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
