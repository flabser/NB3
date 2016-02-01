package kz.flabs.scriptprocessor.page.doscript;

import groovy.lang.GroovyObject;

import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.Vocabulary;
import kz.flabs.users.User;
import kz.flabs.util.XMLResponse;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class DoProcessor {

	private String lang;
	private GroovyObject groovyObject = null;
	private _Session ses;
	private Vocabulary vocabulary;
	private _WebFormData webFormData;

	public DoProcessor(AppEnv env, User u, String currentLang, Map<String, String[]> formData, IProcessInitiator init) {
		ses = new _Session(env, u, init);
		vocabulary = env.vocabulary;
		lang = currentLang;
		webFormData = new _WebFormData(formData);
	}

	// @TODO logger
	public XMLResponse processScript(String className) throws ClassNotFoundException {
		try {
			Class pageClass = Class.forName(className);
			groovyObject = (GroovyObject) pageClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		IPageScript myObject = (IPageScript) groovyObject;

		myObject.setSession(ses);
		myObject.setFormData(webFormData);
		myObject.setCurrentLang(vocabulary, lang);

		return myObject.process();

	}

	public XMLResponse processJava(String className, String method) throws ClassNotFoundException {
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

		return myObject.process(method);
	}

}
