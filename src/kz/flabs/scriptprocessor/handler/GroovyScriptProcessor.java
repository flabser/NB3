package kz.flabs.scriptprocessor.handler;

import groovy.lang.GroovyObject;

import java.util.Date;
import java.util.Map;

import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;
import kz.flabs.util.PageResponse;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.lof.appenv.AppEnv;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.pchelka.scheduler.IProcessInitiator;

public class GroovyScriptProcessor extends ScriptProcessor implements Runnable, IProcessInitiator {

	private _Session session;
	private String className;
	private _WebFormData webFormData;

	public GroovyScriptProcessor(AppEnv env, User user, Map<String, String[]> formData) {
		super();
		session = new _Session(env, user);
		webFormData = new _WebFormData(formData, "");
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public PageResponse runScript() throws ClassNotFoundException {
		GroovyObject groovyObject = null;
		PageResponse xmlResp = null;
		try {
			Class querySaveClass = Class.forName(className);
			groovyObject = (GroovyObject) querySaveClass.newInstance();
			kz.flabs.scriptprocessor.handler.IHandlerScript myObject = (kz.flabs.scriptprocessor.handler.IHandlerScript) groovyObject;

			myObject.setSession(session);
			myObject.setWebFormData(webFormData);
			return myObject.run();
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
	public void run() {
		try {
			runScript();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}

}
