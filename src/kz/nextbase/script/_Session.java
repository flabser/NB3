package kz.nextbase.script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LanguageType;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.caching.ICache;
import kz.flabs.runtimeobj.page.Page;
import kz.flabs.users.User;
import kz.flabs.webrule.GlobalSetting;
import kz.lof.user.AuthModeType;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script.actions._ActionBar;
import kz.nextbase.script.mail._MailAgent;
import kz.pchelka.scheduler.IProcessInitiator;
import net.sf.saxon.s9api.SaxonApiException;

public class _Session extends _ScriptingObject implements ICache {

	private IDatabase dataBase;
	private User user;
	private AppEnv env;
	private String formSesID;
	private IProcessInitiator initiator;
	private LanguageType lang;
	public int pageSize = 30;
	private AuthModeType authMode;
	private ArrayList<_Session> descendants = new ArrayList<_Session>();

	@Deprecated
	public _Session(AppEnv env, User user, IProcessInitiator init) {
		this.env = env;
		this.user = user;
		setInitiator(init);
		dataBase = env.getDataBase();
	}

	public _Session(AppEnv env, User user) {
		this.env = env;
		this.user = user;
		dataBase = env.getDataBase();
	}

	public _AppEntourage getAppEntourage() {
		return new _AppEntourage(this, env);
	}

	public GlobalSetting getGlobalSettings() {
		return env.globalSetting;
	}

	public void setLang(LanguageType lang) {
		this.lang = lang;

	}

	public LanguageType getLang() {
		return lang;

	}

	public _ActionBar createActionBar() {
		return new _ActionBar(this);
	}

	public _ViewEntryCollectionParam createViewEntryCollectionParam() {
		return new _ViewEntryCollectionParam(this);
	}

	public IDatabase getCurrentDatabase() {
		return dataBase;
	}

	public _UserActivity getUserActivity() {
		return new _UserActivity(dataBase, user);
	}

	public _MailAgent getMailAgent() {
		return new _MailAgent(this);
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return "userid=" + user.getUserID() + ", database=" + dataBase.toString();
	}

	public String getFormSesID() {
		return formSesID;
	}

	public void setFormSesID(String formSesID) {
		this.formSesID = formSesID;
	}

	public Set<String> getExpandedThread() {
		return new HashSet<String>();
	}

	public IProcessInitiator getInitiator() {
		return initiator;
	}

	public void setInitiator(IProcessInitiator initiator) {
		this.initiator = initiator;
	}

	public _Employer getCurrentAppUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public PageOutcome getCachedPage(Page page, HashMap<String, String[]> formData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, QueryFormulaParserException,
	        DocumentException, DocumentAccessException, QueryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageOutcome getCachedPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, IOException,
	        SaxonApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	public String getCacheInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	private void setPageSize(int ps) {
		pageSize = ps;
	}

	public int getPageSize() {
		return pageSize;
	}

	public _Session clone(AppEnv env) {
		_Session newSes = new _Session(env, user);
		newSes.authMode = AuthModeType.LOGIN_THROUGH_TOKEN;
		newSes.setLang(lang);
		newSes.setPageSize(pageSize);
		descendants.add(newSes);
		return newSes;
	}

}
