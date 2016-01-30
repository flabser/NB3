package kz.flabs.scriptprocessor.form.querysave;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.forum.Topic;
import kz.flabs.scriptprocessor.ScriptProcessorUtil;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.RunMode;
import kz.nextbase.script.*;
import kz.nextbase.script.forum._Topic;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.struct._Department;
import kz.nextbase.script.struct._Employer;
import kz.nextbase.script.struct._Organization;
import kz.nextbase.script.struct._UserGroup;
import kz.nextbase.script.task._Task;
import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.IProcessInitiator;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class QuerySaveProcessor implements IProcessInitiator {
	public ArrayList<IQuerySaveTransaction> transactionToPost = new ArrayList<IQuerySaveTransaction>();

	private String script;	
	private GroovyObject groovyObject = null;
	private _Session ses;
	private _Document doc;
	private String user;
	private Vocabulary vocabulary;
	private String currentLang;
	private _WebFormData webFormData;
	
	@Deprecated
	public QuerySaveProcessor(AppEnv env, BaseDocument d, User u, String currentLang){
		user = u.getUserID();
		ses = new _Session(env, u, this);
		ses.getCurrentDatabase().setTransConveyor(transactionToPost);
		if (d instanceof Project){			
			doc = new _Project((Project)d, ses);
		}else if (d instanceof Task){
			doc = new _Task((Task)d, ses);
		}else{
			doc = new _Document(d, ses);
		}	
		vocabulary = env.vocabulary;
		this.currentLang = currentLang;
	}

	public QuerySaveProcessor(AppEnv env, BaseDocument d, User u, String currentLang, HashMap<String, String[]> formData){
		user = u.getUserID();
		ses = new _Session(env, u, this);
		ses.getCurrentDatabase().setTransConveyor(transactionToPost);
		webFormData = new _WebFormData(formData);
		if (d instanceof Project){			
			doc = new _Project((Project)d, ses);
		}else if (d instanceof Topic){
			doc = new _Topic((Topic)d, ses);
		}else if (d instanceof Task){
			doc = new _Task((Task)d, ses);
		}else if (d instanceof Execution){
			doc = new _Execution((Execution)d, ses);
		}else if (d instanceof kz.flabs.runtimeobj.document.glossary.Glossary){
			doc = new _Glossary((kz.flabs.runtimeobj.document.glossary.Glossary)d, ses);
		}else if (d instanceof Employer){
			doc = new _Employer((Employer)d, ses);
		}else if (d instanceof kz.flabs.runtimeobj.document.structure.Department){
			doc = new _Department((kz.flabs.runtimeobj.document.structure.Department)d, ses);
		}else if (d instanceof kz.flabs.runtimeobj.document.structure.Organization){
			doc = new _Organization((kz.flabs.runtimeobj.document.structure.Organization)d, ses);
		}else if (d instanceof kz.flabs.runtimeobj.document.structure.UserGroup){
			doc = new _UserGroup((kz.flabs.runtimeobj.document.structure.UserGroup)d, ses);
		}else{
			doc = new _Document(d, ses);
		}	
		vocabulary = env.vocabulary;
		this.currentLang = currentLang;
		try{
			if (formData.containsKey("formsesid")) {
				String fsid = formData.get("formsesid")[0];
				ses.setFormSesID(fsid);
			}
		}catch(Exception e){
			AppEnv.logger.errorLogEntry(e);
		}
		
	}

	public QuerySaveProcessor(){

	}

	/**@deprecated**/
	public QuerySaveResult doScript() {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
        Class<GroovyObject> groovyClass = null;
        try {
            groovyClass = loader.parseClass(script);
        } catch (org.codehaus.groovy.control.CompilationFailedException compilationFailedException) {
            compilationFailedException.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
			groovyObject = groovyClass.newInstance();
		} catch (InstantiationException e) {					
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		IQuerySaveScript myObject = (IQuerySaveScript) groovyObject;

		myObject.setSession(ses);
		myObject.setDocument(doc);
		myObject.setUser(user);	
		myObject.setCurrentLang(vocabulary, currentLang);
		return myObject.process();		
	}

	@Deprecated
	public QuerySaveResult doScript(Class<GroovyObject> querySaveClass) {		
		try {				
			groovyObject = querySaveClass.newInstance();
		} catch (InstantiationException e) {					
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		IQuerySaveScript myObject = (IQuerySaveScript) groovyObject;

		myObject.setSession(ses);
		myObject.setDocument(doc);
		myObject.setUser(user);	
		myObject.setCurrentLang(vocabulary, currentLang);
		return myObject.process();		
	}



	public QuerySaveResult processScript(String className) throws ClassNotFoundException {		
		try {	
			Class querySaveClass = null;
			ClassLoader parent = this.getClass().getClassLoader();
			CompilerConfiguration compiler = new CompilerConfiguration();
			if (Environment.debugMode == RunMode.ON){		
				compiler.setRecompileGroovySource(true);
				compiler.setMinimumRecompilationInterval(0);
				File curDir = new File("libs");
				File bin = new File("bin");
				String sd = curDir.getAbsolutePath();
				compiler.setTargetDirectory(bin.getAbsolutePath());
				compiler.setClasspath(sd);
//				compiler.setClasspath("C:\\workspace\\NextBase\\bin\\");
				GroovyClassLoader classLoader = new GroovyClassLoader(parent, compiler);
				classLoader.setShouldRecompile(true);
			 	querySaveClass = Class.forName(className, true, classLoader);
			}else{
				querySaveClass = Class.forName(className);
			}
		
			groovyObject = (GroovyObject) querySaveClass.newInstance();
		} catch (InstantiationException e) {					
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		IQuerySaveScript myObject = (IQuerySaveScript) groovyObject;

		myObject.setSession(ses);
		myObject.setDocument(doc);
		myObject.setWebFormData(webFormData);	
		myObject.setCurrentLang(vocabulary, currentLang);
		return myObject.process();		
	}

	public void setScript(String script) {	
		script = normalizeScript(script);		
	}


	public static String normalizeScript(String script) {
		String beforeScript =			
				"import kz.flabs.dataengine.Const;" +
						"import kz.flabs.scriptprocessor.form.querysave.*;" +	
						ScriptProcessorUtil.packageList +
						"class Foo extends AbstractQuerySaveScript{";
		String afterScript = "}";
		return beforeScript + script + afterScript;		
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}
}
