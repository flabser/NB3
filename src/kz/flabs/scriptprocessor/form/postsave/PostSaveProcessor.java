package kz.flabs.scriptprocessor.form.postsave;

import java.util.ArrayList;

import groovy.lang.GroovyObject;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.scriptprocessor.ScriptProcessorUtil;
import kz.flabs.scriptprocessor.form.querysave.IQuerySaveTransaction;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.page.ElementScript;
import kz.nextbase.script._Document;
import kz.nextbase.script._Execution;
import kz.nextbase.script._Session;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.task._Task;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class PostSaveProcessor extends Thread implements IProcessInitiator {
	public ArrayList<IQuerySaveTransaction> transactionToPost = new ArrayList<IQuerySaveTransaction>();

	private ElementScript script;
	private Class<GroovyObject> scriptClass;
	private GroovyObject groovyObject = null;
	private _Session ses;
	private _Document doc;
	private String user;
	private AppEnv env;

	public PostSaveProcessor(BaseDocument d, User u) {
		env = d.getAppEnv();
		IDatabase db = DatabaseFactory.getDatabase(env.appType);
		ses = new _Session(db.getParent(), u, this);
		ses.getCurrentDatabase().setTransConveyor(transactionToPost);
		if (d instanceof Project) {
			doc = new _Project((Project) d, ses);
		} else if (d instanceof Task) {
			doc = new _Task((Task) d, ses);
		} else if (d instanceof Execution) {
			doc = new _Execution((Execution) d, ses);
		} else {
			doc = new _Document(d, ses);
		}
		user = u.getUserID();
	}

	@Override
	public void run() {
		IPostSaveScript myObject = null;
		if (script.getType() == ValueSourceType.JAVA_CLASS) {
			try {
				Class<?> pageClass = Class.forName(script.getClassName());
				myObject = (IPostSaveScript) pageClass.newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				Server.logger.errorLogEntry(e);
			}
		} else {
			try {
				groovyObject = scriptClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			myObject = (IPostSaveScript) groovyObject;
		}
		myObject.setSession(ses);
		myObject.setDocument(doc);
		myObject.setUser(user);
		myObject.setAppEnv(env);
		myObject.process();
	}

	public void setClass(Class<GroovyObject> postSaveClass) {
		this.scriptClass = postSaveClass;
	}

	@SuppressWarnings("unchecked")
	public void setClass(ElementScript s) throws ClassNotFoundException {
		script = s;
		Class postSaveClass = Class.forName(script.getClassName());
		this.scriptClass = postSaveClass;
	}

	public static String normalizeScript(String script) {
		String beforeScript = "import java.util.HashSet;" + "import kz.flabs.dataengine.Const;"
				+ "import kz.flabs.scriptprocessor.form.postsave.*;" + ScriptProcessorUtil.packageList
				+ "class Foo extends AbstractPostSaveScript{";
		String afterScript = "}";
		return beforeScript + script + afterScript;
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}
}
