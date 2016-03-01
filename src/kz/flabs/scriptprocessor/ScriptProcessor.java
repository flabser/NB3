package kz.flabs.scriptprocessor;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.lof.server.Server;
import kz.pchelka.log.ILogger;
import kz.pchelka.scheduler.IProcessInitiator;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;

public class ScriptProcessor implements IScriptProcessor, IProcessInitiator {
	public static ILogger logger = Server.logger;

	@Override
	public String[] processString(String script) {
		Server.logger.errorLogEntry("method 4563 has not reloaded");
		return null;
	}

	@Override
	public String process(String script) {
		Server.logger.errorLogEntry("method 4564 has not reloaded");
		return "";
	}

	@Override
	public String[] processString(Class<GroovyObject> compiledClass) {
		Server.logger.errorLogEntry("method 4565 has not reloaded");
		return null;
	}

	public IScriptSource setScriptLauncher(String userScript, boolean debug) {
		GroovyObject groovyObject = null;
		String script = "";

		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		Class<GroovyObject> groovyClass = null;
		try {
			groovyClass = loader.parseClass(script);

			try {
				groovyObject = groovyClass.newInstance();
			} catch (InstantiationException e) {
				if (!debug) {
					Server.logger.errorLogEntry(e);
				}
			} catch (IllegalAccessException e) {
				if (!debug) {
					Server.logger.errorLogEntry(e);
				}
			}

			IScriptSource sciptObject = (IScriptSource) groovyObject;
			return sciptObject;

		} catch (MultipleCompilationErrorsException mcee) {
			// logger.errorLogEntry(script);
			if (!debug) {
				Server.logger.errorLogEntry(mcee.getMessage());
			}
			return new ScriptSource();
		}
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}

}
