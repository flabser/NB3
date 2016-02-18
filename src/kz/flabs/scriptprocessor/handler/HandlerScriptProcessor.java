package kz.flabs.scriptprocessor.handler;

import groovy.lang.GroovyObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.Vocabulary;
import kz.flabs.scriptprocessor.IScriptSource;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.scriptprocessor.ScriptProcessorType;
import kz.flabs.users.User;
import kz.flabs.util.PageResponse;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.lof.scripting._Session;
import kz.pchelka.scheduler.IProcessInitiator;

@Deprecated
public class HandlerScriptProcessor extends ScriptProcessor implements Runnable, IProcessInitiator {

	private Map<String, String[]> formData = new HashMap<String, String[]>();
	private _Session session;
	private String lang;
	private Vocabulary vocabulary;
	private String className;

	public HandlerScriptProcessor(AppEnv env, User user, String lang) {
		super();
		session = new _Session(env, user);

		vocabulary = env.vocabulary;
		this.lang = lang;
	}

	@Deprecated
	public HandlerScriptProcessor(AppEnv env, User user, Map<String, String[]> formData, String lang) {
		super();
		session = new _Session(env, user);

		if (formData != null) {
			this.formData.putAll(formData);
		}
		vocabulary = env.vocabulary;
		this.lang = lang;
	}

	@Deprecated
	public String process(String script, boolean debug) throws Exception {
		IScriptSource myObject = setScriptLauncher(script, debug);
		myObject.setSession(session);
		myObject.setFormData(formData);

		String result = myObject.providerHandlerProcess();

		return result;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Deprecated
	public PageResponse processScript() throws ClassNotFoundException {
		GroovyObject groovyObject = null;
		PageResponse xmlResp = null;
		try {
			Class querySaveClass = Class.forName(className);
			groovyObject = (GroovyObject) querySaveClass.newInstance();
			kz.flabs.scriptprocessor.handler.IHandlerScript myObject = (kz.flabs.scriptprocessor.handler.IHandlerScript) groovyObject;

			myObject.setSession(session);
			myObject.setFormData(formData);
			myObject.setCurrentLang(lang, vocabulary);
			return myObject.process();
		} catch (Exception e) {
			xmlResp = new PageResponse(ResponseType.RESULT_OF_HANDLER_SCRIPT);
			xmlResp.setResponseStatus(false);
			xmlResp.setMessage(e.toString());
			xmlResp.addMessage(Util.convertDataTimeToString(new Date()));
			logger.errorLogEntry(e);
			return xmlResp;
		}
	}

	@Override
	public String toString() {
		return "type:" + ScriptProcessorType.PROVIDER;
	}

	@Override
	public void run() {
		try {
			processScript();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}

}
