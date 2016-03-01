package kz.flabs.scriptprocessor;

import groovy.lang.GroovyObject;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.users.User;
import kz.lof.scripting._Session;

public class SessionScriptProcessor extends ScriptProcessor {
	private _Session session;

	public SessionScriptProcessor(IDatabase db, User user) {
		super();
		session = new _Session(db.getParent(), user);
	}

	@Override
	public String[] processString(String script) {
		try {
			IScriptSource myObject = setScriptLauncher(script, false);
			myObject.setSession(session);
			return myObject.sessionProcess();
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(script);
			ScriptProcessor.logger.errorLogEntry(e);
			return null;
		}
	}

	@Override
	public String[] processString(Class<GroovyObject> groovyClass) {
		try {
			IScriptSource myObject = (IScriptSource) groovyClass.newInstance();
			myObject.setSession(session);
			return myObject.sessionProcess();
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(e);
			return null;
		}
	}

}
