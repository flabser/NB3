package kz.flabs.scriptprocessor;

import groovy.lang.GroovyObject;
import kz.lof.scripting._Session;

public class SimpleScriptProcessor extends ScriptProcessor {
	private _Session session;

	public SimpleScriptProcessor() {
		super();
	}

	public SimpleScriptProcessor(_Session ses) {
		super();
		session = ses;
	}

	@Override
	public String[] processString(String script) {
		try {
			IScriptSource myObject = setScriptLauncher(script, false);
			myObject.setSession(this.session);
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
			myObject.setSession(this.session);
			return myObject.sessionProcess();
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(e);
			return null;
		}
	}

}
