package kz.flabs.scriptprocessor.form.queryopen;

import java.util.HashMap;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.scriptprocessor.ScriptProcessorUtil;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.constants.RunMode;
import kz.nextbase.script._Document;
import kz.nextbase.script._Execution;
import kz.nextbase.script._Glossary;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.struct._Department;
import kz.nextbase.script.struct._Employer;
import kz.nextbase.script.struct._Organization;
import kz.nextbase.script.struct._UserGroup;
import kz.nextbase.script.task._Task;
import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.IProcessInitiator;

public class QueryOpenProcessor implements IProcessInitiator {
	private GroovyObject groovyObject = null;
	private _Session ses;
	private _Document doc;
	private Vocabulary vocabulary;
	private String currentLang;
	private _WebFormData webFormData;
	private AppEnv env;

	public QueryOpenProcessor(AppEnv env, User u, String currentLang, HashMap<String, String[]> formData) {
		this.env = env;
		ses = new _Session(env, u, this);
		vocabulary = env.vocabulary;
		this.currentLang = currentLang;
		webFormData = new _WebFormData(formData);
	}

	public QueryOpenProcessor(AppEnv env, BaseDocument d, UserSession userSession, HashMap<String, String[]> formData) {
		this.env = env;
		ses = new _Session(env, userSession.currentUser, this);
		vocabulary = env.vocabulary;
		this.currentLang = userSession.lang;
		webFormData = new _WebFormData(formData);
		if (d instanceof Project) {
			doc = new _Project((Project) d, ses);
		} else if (d instanceof Task) {
			doc = new _Task((Task) d, ses);
		} else if (d instanceof Execution) {
			doc = new _Execution((Execution) d, ses);
		} else if (d instanceof Glossary) {
			doc = new _Glossary((Glossary) d, ses);
		} else if (d instanceof Employer) {
			doc = new _Employer((Employer) d, ses);
		} else if (d instanceof kz.flabs.runtimeobj.document.structure.Department) {
			doc = new _Department((kz.flabs.runtimeobj.document.structure.Department) d, ses);
		} else if (d instanceof kz.flabs.runtimeobj.document.structure.UserGroup) {
			doc = new _UserGroup((kz.flabs.runtimeobj.document.structure.UserGroup) d, ses);
		} else if (d instanceof Organization) {
			doc = new _Organization((Organization) d, ses);
		} else {
			doc = new _Document(d, ses);
		}
	}

	public PublishResult processScript(String className) throws ClassNotFoundException {
		try {
			Class queryClass = null;
			ClassLoader parent = this.getClass().getClassLoader();
			CompilerConfiguration compiler = new CompilerConfiguration();
			if (Environment.debugMode == RunMode.ON) {
				compiler.setRecompileGroovySource(true);
				compiler.setMinimumRecompilationInterval(0);
				GroovyClassLoader classLoader = new GroovyClassLoader(parent, compiler);
				classLoader.setShouldRecompile(true);
				queryClass = Class.forName(className, true, classLoader);
			} else {
				queryClass = Class.forName(className);
			}
			groovyObject = (GroovyObject) queryClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		IQueryOpenScript myObject = (IQueryOpenScript) groovyObject;

		myObject.setAppEnv(env);
		myObject.setSession(ses);
		myObject.setFormData(webFormData);
		myObject.setCurrentLang(vocabulary, currentLang);
		if (doc != null) {
			myObject.setDocument(doc);
			return myObject.process2();
		} else {
			return myObject.process1();
		}
	}

	public void setScript(String script) {
		script = normalizeScript(script);
	}

	@Deprecated
	public static String normalizeScript(String script) {
		String beforeScript = "import kz.flabs.dataengine.Const;" + "import kz.flabs.scriptprocessor.form.queryopen.*;"
				+ ScriptProcessorUtil.packageList + "class Foo extends AbstractQueryOpenScript{";
		String afterScript = "}";
		return beforeScript + script + afterScript;
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}
}
