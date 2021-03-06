package kz.flabs.webrule.form;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.lof.appenv.AppEnv;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;

public class ScriptValue {
	public String script;
	public Class<GroovyObject> compiledClass;
	public boolean isCompiled;

	ScriptValue(String script, String description) {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		try {

			isCompiled = true;
		} catch (MultipleCompilationErrorsException e) {
			AppEnv.logger.errorLogEntry("QuerySaveScript compilation error at form rule compiling=" + description + ":" + e.getMessage());
		}
	}

}
