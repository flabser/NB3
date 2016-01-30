package kz.flabs.webrule.form;

import kz.flabs.appenv.AppEnv;
import kz.flabs.scriptprocessor.ScriptProcessor;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

public class ScriptValue {
	public String script;
	public Class<GroovyObject> compiledClass;
	public boolean isCompiled;
	
	ScriptValue(String script, String description){
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		try{
			compiledClass = loader.parseClass(ScriptProcessor.normalizeScript(script));
			isCompiled = true;
		}catch(MultipleCompilationErrorsException e){
			AppEnv.logger.errorLogEntry("QuerySaveScript compilation error at form rule compiling=" + description + ":" + e.getMessage());			
		}
	}
	
	
}
