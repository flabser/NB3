package kz.flabs.scriptprocessor.form.postsave;

import java.util.HashMap;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.scriptprocessor.ScriptEvent;
import kz.flabs.webrule.page.PageRule;
import kz.nextbase.script._Document;
import kz.nextbase.script._Session;

public abstract class AbstractPostSave extends ScriptEvent implements IPostSaveScript {
	private _Session ses;
	private _Document doc;
	private AppEnv env;

	@Override
	public void setSession(_Session ses) {
		this.ses = ses;
	}

	@Override
	public void setDocument(_Document doc) {
		this.doc = doc;
	}

	@Override
	public void setUser(String user) {

	}

	@Override
	public void setAppEnv(AppEnv env) {
		this.env = env;
	}

	public StringBuffer startPage(String id, HashMap<String, String[]> formData)
			throws RuleException, QueryFormulaParserException, ClassNotFoundException, DocumentException,
			DocumentAccessException, QueryException {
		PageRule pageRule = (PageRule) env.ruleProvider.getRule("page", id);
		Page page = new Page(env, ses.getUser().getSession(), pageRule);
		return page.process(formData, "GET");
	}

	@Override
	public void process() {
		try {
			doPostSave(ses, doc);
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	public abstract void doPostSave(_Session ses, _Document doc);

}
